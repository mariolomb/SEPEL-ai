package it.sepel.ai.logic;

import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.TutorChatMessage;
import it.sepel.ai.domain.ELSearch;
import it.sepel.ai.domain.TutorChatSession;
import it.sepel.ai.elasticsearch.ELManager;
import it.sepel.ai.openai.OpenAIManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Manager {

    public static final String CHAT_ROLE_TUTOR = "TUTOR";
    public static final String CHAT_ROLE_USER = "USER";

    public static final Map<String, String> typeAllegatoMap = new HashMap();
    public static final Map<String, String> linkAllegatoMap = new HashMap();

    static {
        typeAllegatoMap.put("QUESITO", "Quesito");
        typeAllegatoMap.put("ARTICOLO_RIVISTA", "Articolo Rivista");
        typeAllegatoMap.put("ARTICOLO_FOCUS", "Articolo Focus");
        typeAllegatoMap.put("NORMATIVA", "Normativa");
        typeAllegatoMap.put("PRASSI", "Prassi");
        typeAllegatoMap.put("GIURISPRUDENZA", "Giurisprudenza");
        typeAllegatoMap.put("VARIE", "Varie");
    }

    static {
        linkAllegatoMap.put("QUESITO", "https://www.sepel.it/viewQuesito?id=");
        linkAllegatoMap.put("ARTICOLO_RIVISTA", "https://www.sepel.it/viewArticoloRivista?id=");
        linkAllegatoMap.put("ARTICOLO_FOCUS", "https://www.sepel.it/viewArticoloFocus?id=");
        linkAllegatoMap.put("NORMATIVA", "https://www.sepel.it/viewNormativa?id=");
        linkAllegatoMap.put("PRASSI", "https://www.sepel.it/viewPrassi?id=");
        linkAllegatoMap.put("GIURISPRUDENZA", "https://www.sepel.it/viewGiurisprudenza?id=");
        linkAllegatoMap.put("VARIE", "");
    }

    @Autowired
    ELManager elManager;

    @Autowired
    OpenAIManager openAIManager;

    private static final Logger log = LoggerFactory.getLogger(Manager.class);

    public List<Contenuto> searchContenuti(ELSearch st) throws Exception {

        //pulizia domanda
        String s = st.getSearchTerm();
        String cleanedInput = s.replaceAll("[\"'\\;:!?]", "");
        cleanedInput = cleanedInput.replaceAll("\\s+", " ").trim();
        st.setSearchTerm(cleanedInput);

        st.setSearchTermEmbedding(generateEmbedding(st.getSearchTerm()));
        return elManager.searchContenuti(st);
    }

    public void processTutorQuery(ELSearch s, TutorChatSession cs) throws Exception {
        TutorChatMessage request = new TutorChatMessage(CHAT_ROLE_USER, s.getSearchTerm());
        cs.getMessages().add(request);
        s.setSearchTerm(s.getSearchTerm().replaceAll("\n", " ").replaceAll("\r", " "));
        List<Contenuto> cc = searchContenuti(s);
        String text = openAIManager.processQuery(s.getSearchTerm(), cc);
        TutorChatMessage response = new TutorChatMessage(CHAT_ROLE_TUTOR, text);

        //con i primi 5 contenuti creo la lista degli allegati
        List<Contenuto> scc = cc.subList(0, Math.min(5, cc.size()));
        scc.removeIf(c -> {
            c.setTesto(""); 
            return c.getScore() <= 10.0;
        });
        response.setAttachments(scc);
        cs.getMessages().add(response);
    }

    public List<Double> generateEmbedding(String testo) {
        return openAIManager.generateEmbedding(testo);
    }

}
