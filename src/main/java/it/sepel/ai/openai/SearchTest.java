package it.sepel.ai.openai;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.ingest.simulate.Document;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import it.sepel.ai.domain.Contenuto;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchTest {

    private static final Logger log = LoggerFactory.getLogger(SearchTest.class);

    private void go(String indexName, String fieldName, String searchTerm) throws Exception {
        
        //crea client per ricerca
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        MatchQuery matchQuery = MatchQuery.of(m -> m
                .field(fieldName)
                .query(searchTerm)
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(q -> q.match(matchQuery))
        );
        
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);

        // Esegue la ricerca
        SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

        // Elabora i risultati
        List<Hit<Map>> hits = response.hits().hits();
        log.info("Trovati " + hits.size() + " documenti:");
        for (Hit<Map> hit : hits) {
            Map document = hit.source();
            log.info(" - Documento trovato: " + document);
        }
    }

    public static void main(String[] args) {
        SearchTest t = new SearchTest();
        try {
            t.go("sepel_ai", "content", "anagrafe");
        } catch (Exception e) {
            log.error("Errore", e);
        }
    }

}
