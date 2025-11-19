package it.sepel.ai.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sepel.ai.AppException;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ELSearch;
import it.sepel.ai.logic.Manager;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ELManager {

    private static final String SEARCH_FIELD = "testo";
    private static final String INDEX_NAME = "sepel_ai";

    private static final Logger log = LoggerFactory.getLogger(ELManager.class);

    @Autowired
    private DocumentRepository documentRepository;
    private final ElasticsearchClient elasticsearchClient;

    public ELManager(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void saveContenuto(Contenuto c) {
        documentRepository.save(c);
    }

    public void deleteContenuto(String id) throws Exception {
        String queryJson = """
             {
               "query": {
                 "bool": {
                   "must": [
                     { "term": { "id": "%s" } }
                   ]
                 }
               }
             }
        """;
        queryJson = queryJson.formatted(id);
        DeleteByQueryRequest.Builder builder = new DeleteByQueryRequest.Builder();
        builder.index(INDEX_NAME);
        builder.withJson(new ByteArrayInputStream(queryJson.getBytes()));
        DeleteByQueryRequest deleteByQueryRequest = builder.build();

        DeleteByQueryResponse response = elasticsearchClient.deleteByQuery(deleteByQueryRequest);
        log.info("Eliminati contenuti [" + response.deleted() + "]");
    }

    public void deleteContenuto(Integer idContenuto, String tipoContenuto) throws Exception {
        String queryJson = """
             {
               "query": {
                 "bool": {
                   "must": [
                     { "term": { "idContenuto": "%d" } },
                     { "term": { "tipoContenuto": "%s" } }
                   ]
                 }
               }
             }
        """;
        queryJson = queryJson.formatted(idContenuto, tipoContenuto);
        DeleteByQueryRequest.Builder builder = new DeleteByQueryRequest.Builder();
        builder.index(INDEX_NAME);
        builder.withJson(new ByteArrayInputStream(queryJson.getBytes()));
        DeleteByQueryRequest deleteByQueryRequest = builder.build();

        DeleteByQueryResponse response = elasticsearchClient.deleteByQuery(deleteByQueryRequest);
        log.info("Eliminati documenti [" + response.deleted() + "]");
    }

    public Contenuto loadContenutoFromELById(String id) throws Exception {
        String queryJson = """
             {
               "query": {
                 "term": {
                   "id": "%s"
                 }
               }
             }
        """;
        queryJson = queryJson.formatted(id);
        SearchRequest searchRequest = new SearchRequest.Builder().withJson(new ByteArrayInputStream(queryJson.getBytes())).build();

        // Esegue la ricerca
        SearchResponse<Contenuto> response = elasticsearchClient.search(searchRequest, Contenuto.class);
        List<Hit<Contenuto>> hits = response.hits().hits();
        log.info("Trovati " + hits.size() + " contenuti:");
        Contenuto ret = null;
        if (!hits.isEmpty()) {
            ret = hits.get(0).source();
        }
        return ret;
    }

    public Contenuto loadContenutoFromELByIdAndTipo(Integer idContenuto, String tipoContenuto) throws Exception {
        String queryJson = """
             {
               "query": {
                 "bool": {
                   "must": [
                     { "term": { "idContenuto": "%d" } },
                     { "term": { "tipoContenuto": "%s" } }
                   ]
                 }
               }
             }
        """;
        queryJson = queryJson.formatted(idContenuto, tipoContenuto);
        SearchRequest searchRequest = new SearchRequest.Builder().withJson(new ByteArrayInputStream(queryJson.getBytes())).build();

        // Esegue la ricerca
        SearchResponse<Contenuto> response = elasticsearchClient.search(searchRequest, Contenuto.class);
        List<Hit<Contenuto>> hits = response.hits().hits();
        //log.info("Trovati " + hits.size() + " contenuti:");
        Contenuto ret = null;
        if (!hits.isEmpty()) {
            ret = hits.get(0).source();
        }
        return ret;
    }

    public List<Contenuto> searchContenuti(ELSearch st) throws Exception {
        // 1) Input & serializzazioni
        List<Double> searchTermEmbedding = st.getSearchTermEmbedding();
        log.info("dimensione searchTermEmbedding [" + (searchTermEmbedding == null ? 0 : searchTermEmbedding.size()) + "]");

        if (st.getRequestedMatters() == null || st.getRequestedMatters().isEmpty()) {
            throw new AppException("Selezionare almeno una materia");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String searchTermEmbeddingString = objectMapper.writeValueAsString(searchTermEmbedding);
        String domandaJson = objectMapper.writeValueAsString(st.getSearchTerm());

        String mattersJsonArray = st.getRequestedMatters().stream()
                .map(m -> "\"" + m + "\"")
                .collect(Collectors.joining(", ", "[", "]"));

        String filterPart = "\"filter\": [ { \"terms\": { \"materia\": " + mattersJsonArray + " } } ],";

        // 2) Query: testo obbligatorio (match OR 60% + match_phrase) + vettore; recency con gauss; rescore forte sul match_phrase
        String queryJson = String.format("""
    {
      "size": 10,
      "query": {
        "function_score": {
          "query": {
            "script_score": {
              "query": {
                "bool": {
                  %s
                  "must": [
                    {
                      "bool": {
                        "should": [
                          {
                            "match": {
                              "testo": {
                                "query": %s,
                                "operator": "OR",
                                "minimum_should_match": "60%%"
                              }
                            }
                          },
                          {
                            "match_phrase": {
                              "testo": {
                                "query": %s,
                                "slop": 3,
                                "boost": 2.0
                              }
                            }
                          }
                        ],
                        "minimum_should_match": 1
                      }
                    }
                  ]
                }
              },
              "script": {
                "source": "(cosineSimilarity(params.q, 'embedding') + 1.0) * params.alpha + _score * params.beta",
                "params": {
                  "q": %s,
                  "alpha": 0.5,
                  "beta": 2.0
                }
              }
            }
          },
          "functions": [
            {
              "gauss": {
                "data": { "origin": "now", "scale": "7300d", "offset": "0d", "decay": 0.95 }
              },
              "weight": 1.0
            }
          ],
          "score_mode": "sum",
          "boost_mode": "multiply"
        }
      },
      "rescore": {
        "window_size": 200,
        "query": {
          "rescore_query": {
            "match_phrase": {
              "testo": {
                "query": %s,
                "slop": 2
              }
            }
          },
          "query_weight": 1.0,
          "rescore_query_weight": 3.0
        }
      }
    }
    """, filterPart, domandaJson, domandaJson, searchTermEmbeddingString, domandaJson);

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("sepel_ai")
                .withJson(new ByteArrayInputStream(queryJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .build();

        SearchResponse<Contenuto> response = elasticsearchClient.search(searchRequest, Contenuto.class);

        List<Contenuto> ret = new ArrayList<>();
        List<Hit<Contenuto>> hits = response.hits().hits();
        log.info("Trovati [" + hits.size() + "] contenuti su EL");
        for (Hit<Contenuto> hit : hits) {
            double score = hit.score() != null ? hit.score() : 0.0;
            Contenuto c = hit.source();
            c.setScores(String.format("%.2f", score));
            c.setScore(score);
            c.setLink(Manager.linkAllegatoMap.get(c.getTipoContenuto()) + c.getIdContenuto());
            c.setTipoContenuto(Manager.typeAllegatoMap.get(c.getTipoContenuto()));
            ret.add(c);
            log.info("Contenuto trovato: Tipo = " + c.getTipoContenuto() + ", ID = " + c.getIdContenuto()
                    + ", Score = " + score + " Data [" + c.getData() + "] Titolo [" + c.getTitolo() + "]");
        }
        return ret;
    }

}


/*
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(INDEX_NAME)
            .query(q -> q.match(matchQuery))
            .sort(sort -> sort
                .field(f -> f
                    .field("data")    // Campo su cui fare l'ordinamento
                    .order(SortOrder.Desc)  // Ordinamento decrescente (più recente prima)
                )
            ).size(5)
            .requestCache(false)
        );
 */
//CODICE DA TESTARE COMUNQUE

/*
MatchQuery matchQuery = MatchQuery.of(m -> m
                .field(SEARCH_FILED)
                .query(st.getSearchTerm())
        );

// Embedding della query (esempio, questo dovrebbe essere un embedding calcolato in precedenza)
List<Double> queryEmbedding = ... // L'array o la lista che contiene l'embedding della query

// Query che utilizza gli embedding
ScriptScoreQuery scriptScoreQuery = ScriptScoreQuery.of(ssq -> ssq
        .query(Query.of(q -> q.match(matchQuery))) // Condizione di match su testo
        .script(sc -> sc
                .source("cosineSimilarity(params.query_embedding, doc['embedding']) + 1.0") // Funzione di similarità coseno
                .params("query_embedding", queryEmbedding) // Passaggio dell'embedding della query
        )
);

// Combinazione delle query con BoolQuery
BoolQuery boolQuery = BoolQuery.of(b -> b
            .should(Query.of(q -> q.match(matchQuery))) // Ricerca di testo
            .should(Query.of(q -> q.script_score(scriptScoreQuery))) // Ricerca sugli embedding
            .minimumShouldMatch("1") // Almeno una delle condizioni deve essere soddisfatta
);

SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("sepel_ai") // Nome dell'indice
            .query(q -> q.bool(boolQuery)) // Combinazione della query booleana
);

 */
