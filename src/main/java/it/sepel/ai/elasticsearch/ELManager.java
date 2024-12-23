package it.sepel.ai.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ELSearch;
import it.sepel.ai.logic.Manager;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
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
    
    public void saveContenuto(Contenuto c) {
        documentRepository.save(c);
    }

    public List<Contenuto> searchContenuti(ELSearch st) throws Exception {
        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        /*
        MatchQuery matchQuery = MatchQuery.of(m -> m
                .field(SEARCH_FILED)
                .query(st.getSearchTerm())
        );
      
        BoolQuery boolQuery = BoolQuery.of(b -> b
                .should(Query.of(q -> q.match(matchQuery)))
                .minimumShouldMatch("1") // Almeno 1 condizione deve essere soddisfatta
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("sepel_ai") // Nome dell'indice
                .query(q -> q.bool(boolQuery))
        );
         */
        List<Double> searchTermEmbedding = st.getSearchTermEmbedding();
        log.info("dimensione searchTermEmbedding [" + searchTermEmbedding.size() + "]");
        ObjectMapper objectMapper = new ObjectMapper();
        String searchTermEmbeddingString = objectMapper.writeValueAsString(searchTermEmbedding);
        
        String queryJson = """
            {
              "query": {
                "bool": {
                  "should": [
                       {
                      "match": {
                         "testo": "%s"
                      }
                    },
                    {
                      "script_score": {
                        "query": {
                          "match_all": {}
                        },
                        "script": {
                          "source": "cosineSimilarity(params.query_embedding, 'embedding')+ 1.0",
                          "params": {
                            "query_embedding": """ + searchTermEmbeddingString + """
                          }
                        }
                      }
                    }
                  ],
                  "minimum_should_match": 1
                }
              }
            }
        """;
        queryJson = queryJson.formatted(st.getSearchTerm());
        //log.info("json = [" + queryJson + "]");
        
        SearchRequest searchRequest = new SearchRequest.Builder().withJson(new ByteArrayInputStream(queryJson.getBytes())).build();
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);

        // Esegue la ricerca
        SearchResponse<Contenuto> response = elasticsearchClient.search(searchRequest, Contenuto.class);

        List<Contenuto> ret = new ArrayList();
        List<Hit<Contenuto>> hits = response.hits().hits();
        log.info("Trovati " + hits.size() + " contenuti:");
        for (Hit<Contenuto> hit : hits) {
            double score = hit.score() != null ? hit.score() : 0.0;
            Contenuto c = hit.source();
            c.setScores(String.format("%.2f", score));
            c.setScore(score);
            c.setLink(Manager.linkAllegatoMap.get(c.getTipoContenuto()) + c.getIdContenuto());
            c.setTipoContenuto(Manager.typeAllegatoMap.get(c.getTipoContenuto()));
            ret.add(c);
            log.info("Contenuto trovato: Tipo = " + c.getTipoContenuto() + ", ID = " + c.getIdContenuto() + ", Score = " + score);
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