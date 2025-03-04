 package it.sepel.ai.task;

import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ContenutoSearch;
import it.sepel.ai.elasticsearch.ELManager;
import it.sepel.ai.mapper.ApplicationMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DeleteContenuti {
    
    private static final Logger log = LoggerFactory.getLogger(DeleteContenuti.class);
    
    @Autowired
    ApplicationMapper mapper;
    
    @Autowired
    ELManager elManager;
    
    //ogni minuto verifica se ci sono dei contenuti da cancellare li elimina da EL e setta lo stato a cancellati
    @Scheduled(fixedRate = 60000)
    public void execute() {
        try {
            log.info("Loading contenuti to delete");
            ContenutoSearch cs = new ContenutoSearch();
            cs.setStato("TO_DELETE");
            List<Contenuto> cc = mapper.searchContenuti(cs);
            log.info("Found [" + cc.size() + "] contenuti to delete");
            for(Contenuto c: cc) {
                elManager.deleteContenuto(c.getId());
                mapper.deleteContenuto(c.getId());
            }
        } catch (Exception e) {
            log.error("ERRORE cancellazione contenuti from EL", e);
        }
    }
}
