package it.sepel.ai.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

    private static final Logger log = LoggerFactory.getLogger(IndexService.class);
    private final ElasticsearchClient elasticsearchClient;
    
    /*
    
    //canella indice
    curl -X DELETE "http://localhost:9200/sepel_ai"
    
    //crea indice con embed
    curl -X PUT "http://localhost:9200/sepel_ai" -H "Content-Type: application/json" -d'
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "idContenuto": { "type": "integer" },
      "testo": { "type": "text" },
      "tipoContenuto": { "type": "keyword" },
      "materia": { "type": "keyword" },
      "data": { "type": "date" },
      "embedding": { "type": "dense_vector", "dims": 1536 }
    }
  }
}'
    
    */
    @Autowired
    public IndexService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void deleteIndex(String indexName) {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();
            if (exists) {
                elasticsearchClient.indices().delete(d -> d.index(indexName));
                log.info("Indice eliminato con successo: " + indexName);
            } else {
                log.info("L'indice non esiste: " + indexName);
            }
        } catch (ElasticsearchException | IOException e) {
            log.error("Errore durante l'eliminazione dell'indice", e);
        }
    }

    public void createIndex(String indexName) {
        try {
            elasticsearchClient.indices().create(c -> c.index(indexName));
            System.out.println("Indice creato con successo: " + indexName);
        } catch (ElasticsearchException | IOException e) {
            log.error("Errore durante la creazione dell'indice", e);
        }
    }
}
