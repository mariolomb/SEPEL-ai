package it.sepel.ai.logic;

import it.sepel.acl2sb.domain.LoginResult;
import it.sepel.ai.domain.AIResponse;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.TutorChatMessage;
import it.sepel.ai.domain.ELSearch;
import it.sepel.ai.domain.ItemCronologia;
import it.sepel.ai.domain.Rating;
import it.sepel.ai.domain.TutorChatSession;
import it.sepel.ai.elasticsearch.ELManager;
import it.sepel.ai.mapper.ApplicationMapper;
import it.sepel.ai.openai.OpenAIManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class Manager {

    public static final String SYSTEM_ROLE = "system";
    public static final String USER_ROLE = "user";

    public static final Map<String, String> typeAllegatoMap = new HashMap();
    public static final Map<String, String> linkAllegatoMap = new HashMap();

    static {
        typeAllegatoMap.put("QUESITO", "Quesito");
        typeAllegatoMap.put("ARTICOLO_RIVISTA", "Articolo Rivista");
        typeAllegatoMap.put("ARTICOLO_FOCUS", "Articolo Focus");
        typeAllegatoMap.put("NORMATIVA", "Normativa");
        typeAllegatoMap.put("PRASSI", "Prassi");
        typeAllegatoMap.put("GIURISPRUDENZA", "Giurisprudenza");
        typeAllegatoMap.put("CORSO", "Corso");
        typeAllegatoMap.put("MODULO_GEMO", "Modulistica");
        typeAllegatoMap.put("VARIE", "Varie");
    }

    static {
        linkAllegatoMap.put("QUESITO", "https://www.sepel.it/viewQuesito?id=");
        linkAllegatoMap.put("ARTICOLO_RIVISTA", "https://www.sepel.it/viewArticoloRivista?id=");
        linkAllegatoMap.put("ARTICOLO_FOCUS", "https://www.sepel.it/viewArticoloFocus?id=");
        linkAllegatoMap.put("NORMATIVA", "https://www.sepel.it/viewNormativa?id=");
        linkAllegatoMap.put("PRASSI", "https://www.sepel.it/viewPrassi?id=");
        linkAllegatoMap.put("GIURISPRUDENZA", "https://www.sepel.it/viewGiurisprudenza?id=");
        linkAllegatoMap.put("CORSO", "https://www.sepel.it/viewCorso?id=");
        linkAllegatoMap.put("MODULO_GEMO", "https://www.sepel.it/viewModulistica?id=");
        linkAllegatoMap.put("VARIE", "https://www.sepel.it/file/");
    }

    @Autowired
    ELManager elManager;

    @Autowired
    OpenAIManager openAIManager;

    @Autowired
    ApplicationMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(Manager.class);

    public MultipartFile convertFileToMultipartFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile(
                file.getName(),
                file.getName(),
                "application/pdf",
                input
        );
    }

    public String extractTextFromPDF(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        }
    }

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
        TutorChatMessage newUserMessage = new TutorChatMessage(USER_ROLE, s.getSearchTerm());
        TutorChatMessage lastUserMessage = cs.getLastUserMessage();
        TutorChatMessage lastSystemMessage = cs.getLastSystemMessage();

        //verifico se è un chiarimento della domanda precedente o una nuova domanda
        Boolean isChiarimento = openAIManager.isClarificationOrNewQuestion(lastUserMessage, lastSystemMessage, newUserMessage);
        
        AIResponse r;
        if (!isChiarimento) {
            s.setSearchTerm(s.getSearchTerm().replaceAll("\n", " ").replaceAll("\r", " "));
            List<Contenuto> cc = searchContenuti(s);
            r = openAIManager.processQuery(s.getSearchTerm(), cc);
            r.setChiarimento("NO");
            TutorChatMessage response = new TutorChatMessage(SYSTEM_ROLE, r);

            //con i primi 5 contenuti creo la lista degli allegati
            List<Contenuto> scc = cc.subList(0, Math.min(5, cc.size()));
            response.setAttachments(scc);
            cs.addMessage(response);
        } else {
            r = openAIManager.processQuery(cs, s.getSearchTerm());
            r.setChiarimento("SI");
            TutorChatMessage response = new TutorChatMessage(SYSTEM_ROLE, r);
            cs.addMessage(response);
        }

        //aggiungo il nuovo messaggio alla sessione
        cs.addMessage(newUserMessage);

        Subject currentUser = SecurityUtils.getSubject();
        LoginResult lr = (LoginResult) currentUser.getPrincipal();
        r.setCodice(lr.getCodice());
        r.setIdAbbonato(lr.getIdAbbonato());
        r.setPrompt(s.getSearchTerm());
        r.setSessionId(cs.getId());
        mapper.logRequest(r);

    }

    
    /*
    public void processTutorQuery(ELSearch s, TutorChatSession cs) throws Exception {
        TutorChatMessage newUserMessage = new TutorChatMessage(USER_ROLE);

        //verifico se è un chiarimento della domanda precedente o una nuova domanda
        Boolean isChiarimento = openAIManager.isClarificationOrNewQuestion(lastUserMessage, lastSystemMessage, newUserMessage);
        
        AIResponse r;
        TutorChatMessage response;
        if (!isChiarimento) {
            s.setSearchTerm(s.getSearchTerm().replaceAll("\n", " ").replaceAll("\r", " "));
            List<Contenuto> cc = searchContenuti(s);

            r = openAIManager.processQuery(s.getSearchTerm(), cc);
            response = new TutorChatMessage(SYSTEM_ROLE, r);

            //con i primi 5 contenuti creo la lista degli allegati
            List<Contenuto> scc = cc.subList(0, Math.min(5, cc.size()));
            response.setAttachments(scc);
        } else {
            r = openAIManager.processQuery(cs, s.getSearchTerm());
            response = new TutorChatMessage(SYSTEM_ROLE, r);
        }
        cs.addMessage(response);

        //aggiungo il nuovo messaggio alla sessione
        //cs.addMessage(newUserMessage);

        Subject currentUser = SecurityUtils.getSubject();
        LoginResult lr = (LoginResult) currentUser.getPrincipal();
        r.setCodice(lr.getCodice());
        r.setIdAbbonato(lr.getIdAbbonato());
        r.setPrompt(s.getSearchTerm());
        mapper.logRequest(r);
    }
    */

    public List<Double> generateEmbedding(String testo) throws Exception {
        return openAIManager.generateEmbedding(testo);
    }

    public List<ItemCronologia> loadCronologia(String codiceAbbonato) {
        return mapper.loadCronologia(codiceAbbonato);
    }

    public List<String> loadMaterie() {
        return mapper.loadMaterie();
    }
    
    public void updateRating(Rating r) {
        mapper.updateRating(r);
    }
}
