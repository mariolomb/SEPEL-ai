package it.sepel.ai.openai;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import it.sepel.ai.domain.Contenuto;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpenAIManager {

    
    //il parametro API_KEY è impostato in una variabile di ambiente
    //altrimenti se va su github viene revocata per sicurezza
    //per impostare la variabile scriverla nel file .bashrc
    //export OPENAI_API_KEY=
    //private final static String API_KEY = "";
    private static final Logger log = LoggerFactory.getLogger(OpenAIManager.class);
    
    private final OpenAiService openAiService;

    public OpenAIManager() {
        String API_KEY = System.getenv("OPENAI_API_KEY");
        this.openAiService = new OpenAiService(API_KEY,Duration.ofSeconds(120));
    }

    public String processQuery(String userQuery, List<Contenuto> cc) {

        //crea il context mettendo assieme tutti i testi dei contenuti
        StringBuilder contenutiRilevanti = new StringBuilder();
        for (Contenuto c : cc) {
            contenutiRilevanti.append(c.getTesto());
        }

        //if(true) return "";
        /*
        String prompt = String.format("""
            Ecco alcune informazioni rilevanti dai documenti:
            %s

            Domanda: %s
            Risposta:
            """, context.toString(), userQuery);
        */

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                //.model("gpt-3.5-turbo") // O "gpt-3.5-turbo"
                .model("gpt-4o") 
                .messages(List.of(
                        new ChatMessage("system", "Sei un assistente che risponde alle domande che ti pongono gli abbonati"
                                + "al portale Lo stato civile Italiano. Usa prima di tutto i documenti forniti per rispondere alle domande e fai riferimento"
                                + "ad essi nelle risposte senza utilizzare la frase Secondo i documenti forniti"),
                        new ChatMessage("user", "Ecco i documenti rilevanti: " + contenutiRilevanti.toString()),
                        new ChatMessage("user", "Ora rispondi a questa domanda: " + userQuery)
                        
                        //new ChatMessage("system", "Sei un assistente in materie di stato civile, anagrafe, elettorale, polizia mortuarie e documentazione amministrativa"
                        //        + "e fornisci riferimenti normativi in ogni risposta"),
                        //new ChatMessage("user", prompt)
                ))
                .maxTokens(1000)
                .temperature(0.2)
                .build();

        
        return openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
    }

    public List<Double> generateEmbedding(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                .input(List.of(text))
                .build();
        List<Embedding> embeddings = openAiService.createEmbeddings(request).getData();
        List<Double> embeddingVector = embeddings.get(0).getEmbedding();
        return embeddingVector;
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
