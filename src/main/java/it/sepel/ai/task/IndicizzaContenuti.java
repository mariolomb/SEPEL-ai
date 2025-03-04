package it.sepel.ai.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ContenutoSearch;
import it.sepel.ai.elasticsearch.ELManager;
import it.sepel.ai.mapper.ApplicationMapper;
import it.sepel.ai.openai.OpenAIManager;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IndicizzaContenuti {

    private static final Logger log = LoggerFactory.getLogger(IndicizzaContenuti.class);

    @Autowired
    ApplicationMapper mapper;

    @Autowired
    ELManager elManager;

    @Autowired
    OpenAIManager openAiManager;

    //ogni minuto verifica se ci sono dei contenuti da indicizzare con fixDelay una esecuzione aspetta che
    //la precedente eventualmente ancora in esecuzione finisca prima di partire. se non fosse cosi
    //potrebbero girare 2 esecuzioni sugli stessi dati (se la prima ci mette piu di 60 secondi come
    //sulle indicizzazioni massive)
    @Scheduled(fixedDelay = 60000)
    public void execute() {
        log.info("Loading contenuti to index");
        ObjectMapper objectMapper = new ObjectMapper();
        ContenutoSearch cs = new ContenutoSearch();
        cs.setStato("TO_INDEX");
        List<Contenuto> cc = mapper.searchContenuti(cs);
        log.info("Found [" + cc.size() + "] contenuti to index");
        for (Contenuto c : cc) {
            c.setStato("INDEXED");
            c.setEsitoIndicizzazione("OK");
            try {
                List<Double> embedding = openAiManager.generateEmbedding(c.getTesto());
                c.setEmbedding(embedding);
                elManager.saveContenuto(c);
                String embeddingString = objectMapper.writeValueAsString(c.getEmbedding());
                c.setEmbeddingString(embeddingString);
            } catch (Exception e) {
                log.error("ERRORE indicizzazione contenuto in EL", e);
                c.setStato("ERROR");
                c.setEsitoIndicizzazione(e.getMessage());
            }
            mapper.indicizzaContenuto(c);
        }
    }
}
