package it.sepel.ai.listener;

import it.sepel.ai.elasticsearch.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {


    private static final Logger log = LoggerFactory.getLogger(StartupListener.class);
    private static final String PDF_FILE_PATH = "/home/mario/Scrivania/db/quesiti";
    
    @Autowired
    private Indexer indexer;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("application started!");
        try {
            
            if(true) return;
            
            
            //indexer.go();
            
            /*
            //carica tutti gli statiConvenzione
            List<StatoConvenzioni> ssc = mapperStatoConvenzioni.selectAll();
            for(StatoConvenzioni sc: ssc) {
                String nomeStato = sc.getStato();
                nomeStato = nomeStato.trim();
                Stato s = mapperStato.selectByNomeStato(sc.getStato());
                if(s!=null) {
                    sc.setIdStato(s.getIdStato());
                    mapperStatoConvenzioni.updateByNomeStato(sc);
                } else {
                    log.info("Not updated for stato [" + sc.getStato() + "]");
                }
            }
            */
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
