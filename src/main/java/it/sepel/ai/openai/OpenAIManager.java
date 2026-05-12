package it.sepel.ai.openai;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import it.sepel.ai.domain.AIResponse;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.TutorChatMessage;
import it.sepel.ai.domain.TutorChatSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import retrofit2.HttpException;

@Service
@DependsOn("loadAppConfig")
public class OpenAIManager {

    //private final static String API_KEY = "";
    //private final static String OPEN_AI_MODEL = "gpt-4-turbo";
    private final static String OPEN_AI_MODEL_4O = "gpt-4o";
    private final static String OPEN_AI_MODEL_4O_MINI = "gpt-4o-mini";

    //obsoleto da eliminare
    //private final static String OPEN_AI_EMBEDDING_MODEL = "text-embedding-ada-002";
    //aggiornato a
    private static final String OPEN_AI_EMBEDDING_MODEL = "text-embedding-3-small";

    private static final Logger log = LoggerFactory.getLogger(OpenAIManager.class);

    private OpenAiService openAiService;

    private OpenAiService getOpenAiService() {
        if (this.openAiService == null) {
            String API_KEY = System.getProperty("OPENAI_API_KEY");
            if (StringUtils.isEmpty(API_KEY)) {
                throw new IllegalStateException("OPENAI_API_KEY non è valorizzata");
            }
            this.openAiService = new OpenAiService(API_KEY, Duration.ofSeconds(120));
        } else {
            //log.info("OpenAiService is initialized");
        }
        return this.openAiService;
    }

    private String getContenutiRilevanti(List<Contenuto> cc) throws Exception {
        StringBuilder contenutiRilevanti = new StringBuilder();
        Integer numToken = 0;
        for (Contenuto c : cc) {
            String testo = c.getTesto();
            Integer numTokenTesto = getRquestNumToken(testo);
            if (numToken + numTokenTesto > 200000) {
                break;
            }
            contenutiRilevanti.append(testo);
            numToken = numToken + numTokenTesto;
        }
        return contenutiRilevanti.toString();
    }

    public AIResponse processQuery(TutorChatSession s, String lastUserQuery) throws Exception {
        String OPEN_AI_MODEL = OPEN_AI_MODEL_4O;
        TutorChatMessage lastMessage = s.getLastUserMessage();
        String contenutiRilevanti = "";

        List<ChatMessage> cmm = new ArrayList();
        cmm.add(new ChatMessage("system", "Sei un assistente che risponde alle domande che ti pongono gli abbonati"
                + "al portale Lo stato civile Italiano. Usa prima di tutto i documenti forniti per rispondere alle domande e fai riferimento"
                + "ad essi nelle risposte senza utilizzare la frase Secondo i documenti forniti"));

        //se è un chiarimento mando tutta la lista dei messaggi con relativi contenuti
        for (TutorChatMessage m : s.getMessages()) {
            ChatMessage cm = new ChatMessage(m.getRole(), m.getText());
            cmm.add(cm);

            //prendo gli ultimi contenuti
            if (!m.getAttachments().isEmpty()) {
                contenutiRilevanti = contenutiRilevanti + getContenutiRilevanti(m.getAttachments());
            }
        }

        //calcola numero dei token
        Integer numToken = getRquestNumToken(contenutiRilevanti);
        log.info("Num token totali richiesta [" + numToken + "]");
        if (numToken > 28000) {
            OPEN_AI_MODEL = OPEN_AI_MODEL_4O_MINI;
        }

        log.info("Sto usando il modello [" + OPEN_AI_MODEL + "]");
        cmm.add(new ChatMessage("user", "La domanda precedente è : " + lastMessage.getText()));
        cmm.add(new ChatMessage("user", "Ecco i documenti rilevanti: " + contenutiRilevanti));
        cmm.add(new ChatMessage("user", "Ora rispondi a questa domanda considerando il fatto che è un chiarimento rispetto alla"
                + "domanda precedente: " + lastUserQuery));
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(OPEN_AI_MODEL)
                .messages(cmm)
                .maxTokens(1000)
                .temperature(0.2)
                .build();
        ChatCompletionResult result = getOpenAiService().createChatCompletion(request);
        String assistantResponse = result.getChoices().get(0).getMessage().getContent();
        Usage u = result.getUsage();
        AIResponse ret = new AIResponse();
        ret.setResponse(assistantResponse);
        ret.setPromptTokens(u.getPromptTokens());
        ret.setCompletionTokens(u.getCompletionTokens());
        ret.setTotalTokens(u.getTotalTokens());
        ret.setId(result.getId());
        return ret;
    }

    public AIResponse processQuery(String userQuery, List<Contenuto> cc) throws Exception {

        String OPEN_AI_MODEL = OPEN_AI_MODEL_4O;
        String contenutiRilevanti = getContenutiRilevanti(cc);
        Integer numToken = getRquestNumToken(contenutiRilevanti);
        log.info("Num token totali richiesta [" + numToken + "]");
        if (numToken > 28000) {
            OPEN_AI_MODEL = OPEN_AI_MODEL_4O_MINI;
        }
        log.info("Sto usando il modello [" + OPEN_AI_MODEL + "]");
        ChatMessage m = new ChatMessage();
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(OPEN_AI_MODEL)
                .messages(List.of(
                        new ChatMessage("system", "Sei un assistente che risponde alle domande che ti pongono gli abbonati"
                                + "al portale Lo stato civile Italiano. Usa prima di tutto i documenti forniti per rispondere alle domande e fai riferimento"
                                + "ad essi nelle risposte senza utilizzare la frase Secondo i documenti forniti"),
                        new ChatMessage("user", "Ecco i documenti rilevanti: " + contenutiRilevanti),
                        new ChatMessage("user", "Ora rispondi a questa domanda: " + userQuery)
                ))
                .maxTokens(1000)
                .temperature(0.2)
                .build();
        ChatCompletionResult result = getOpenAiService().createChatCompletion(request);
        String assistantResponse = result.getChoices().get(0).getMessage().getContent();
        Usage u = result.getUsage();
        AIResponse ret = new AIResponse();
        ret.setResponse(assistantResponse);
        ret.setPromptTokens(u.getPromptTokens());
        ret.setCompletionTokens(u.getCompletionTokens());
        ret.setTotalTokens(u.getTotalTokens());
        ret.setId(result.getId());
        return ret;
    }

    public List<Double> generateEmbedding(String text) throws Exception {
        if (StringUtils.isEmpty(text)) {
            throw new Exception("Il testo per embedding non può essere vuoto");
        }
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model(OPEN_AI_EMBEDDING_MODEL)
                .input(List.of(text))
                .build();
        try {
            List<Embedding> embeddings = getOpenAiService().createEmbeddings(request).getData();
            List<Double> embeddingVector = embeddings.get(0).getEmbedding();
            log.info("Embedding generato by openAI");
            return embeddingVector;
        } catch (HttpException e) {
            String errorBody = e.response() != null && e.response().errorBody() != null
                    ? e.response().errorBody().string()
                    : "no error body";
            log.error("OpenAI 400 error: {}", errorBody);
            throw new RuntimeException("Errore OpenAI: " + errorBody, e);
        }
    }

    public Boolean isClarificationOrNewQuestion(TutorChatMessage lastUserMessage, TutorChatMessage lastSystemMessage, TutorChatMessage newUserMessage) {
        Boolean ret = false;
        if (null != lastUserMessage && null != newUserMessage) {
            String previousQuestion = lastUserMessage.getText() == null
                    ? ""
                    : lastUserMessage.getText();
            String previousAnswer = lastSystemMessage != null
                    && lastSystemMessage.getText() != null
                    ? lastSystemMessage.getText()
                    : "";
            String newQuestion = newUserMessage.getText() == null
                    ? ""
                    : newUserMessage.getText();
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(OPEN_AI_MODEL_4O_MINI)
                    .messages(List.of(
                            new ChatMessage("system",
                                    "Sei un assistente che deve capire se l'ultima domanda dell'utente è un chiarimento "
                                    + "rispetto alla domanda precedente oppure una nuova domanda. "
                                    + "Rispondi solo con chiarimento o nuova domanda"),
                            new ChatMessage("user",
                                    "Questa è la domanda precedente: " + previousQuestion),
                            new ChatMessage("user",
                                    "Questa è la risposta a quella domanda: " + previousAnswer),
                            new ChatMessage("user",
                                    "Questa è la nuova domanda: " + newQuestion)
                    ))
                    .maxTokens(10)
                    .temperature(0.0)
                    .build();
            ChatCompletionResult result = getOpenAiService().createChatCompletion(request);
            String assistantResponse = result.getChoices().get(0).getMessage().getContent();
            log.info("è un chiarimento = [{}]", assistantResponse);
            String normalized = assistantResponse == null ? "" : assistantResponse.trim().toLowerCase();
            ret = normalized.contains("chiarimento");
        }
        return ret;
    }

    /*
    public Boolean isClarificationOrNewQuestion(TutorChatMessage lastUserMessage, TutorChatMessage lastSystemMessage, TutorChatMessage newUserMessage) {
        Boolean ret = false;
        if (null != lastUserMessage) {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(OPEN_AI_MODEL_4O)
                    .messages(List.of(
                            new ChatMessage("system", "Sei un assistente che deve capire se l'ultima domanda dell'utente è un chiarimento"
                                    + "rispetto alla domanda precedente oppure una nuova domanda. Rispondi solo con chiarimento o nuova domanda"),
                            new ChatMessage("user", "Questa è la domanda precedente: " + lastUserMessage.getText()),
                            new ChatMessage("user", "Questa è la risposta a quella domanda : " + lastSystemMessage.getText()),
                            new ChatMessage("user", "Questa è la nuova domanda: " + newUserMessage.getText())
                    ))
                    .maxTokens(1000)
                    .temperature(0.2)
                    .build();
            ChatCompletionResult result = getOpenAiService().createChatCompletion(request);
            String assistantResponse = result.getChoices().get(0).getMessage().getContent();
            log.info("è un chiarimento = [" + assistantResponse + "]");
            if (assistantResponse.contains("Chiarimento")) {
                ret = true;
            }
        }
        return ret;
    }
     */

    //metodo che torna il numero di token associati ad un testo usando un
    //programma python esterno
    public Integer getRquestNumToken(String inputText) throws Exception {
        Path tempFile = Files.createTempFile("token_input", ".txt");

        try {
            Files.write(tempFile, inputText.getBytes(), StandardOpenOption.WRITE);
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "/opt/applicazioni/SEPEL-ai/count_tokens2.py", tempFile.toString());
            Process process = processBuilder.start();

            // Legge l'output dello script Python
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Integer numToken = 0;
            while ((line = reader.readLine()) != null) {
                numToken = Integer.valueOf(line);
            }
            process.waitFor();
            log.info("Num token per la richiesta [" + numToken + "]");
            return numToken;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}

/*
// Supponiamo che tu abbia una lista di documenti rilevanti
List<String> relevantDocuments = List.of(
    "Documento 1: Questo documento parla di X.",
    "Documento 2: Questo documento riguarda Y."
);

// Aggiungi i documenti rilevanti al contesto
String context = relevantDocuments.stream()
        .collect(Collectors.joining("\n"));

// Crea il messaggio iniziale per includere i documenti
ChatSession chatSession = new ChatSession();
chatSession.addMessage("system", "Sei un assistente intelligente.");
chatSession.addMessage("system", "I documenti rilevanti per questa domanda sono:\n" + context);

// Aggiungi la domanda dell'utente
chatSession.addMessage("user", "Puoi dirmi di più su X?");

// Crea la richiesta
ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo")
        .messages(chatSession.getMessages())
        .maxTokens(500)
        .temperature(0.7)
        .build();

// Effettua la chiamata
var response = service.createChatCompletion(request);

// Aggiungi la risposta del modello alla sessione
String assistantResponse = response.getChoices().get(0).getMessage().getContent();
chatSession.addMessage("assistant", assistantResponse);

// Stampa la risposta
System.out.println("Risposta: " + assistantResponse);

 */
