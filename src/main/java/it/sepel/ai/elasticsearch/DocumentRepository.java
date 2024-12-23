package it.sepel.ai.elasticsearch;

import it.sepel.ai.domain.Contenuto;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends ElasticsearchRepository<Contenuto, String> {
    // Puoi aggiungere query personalizzate se necessario
}
