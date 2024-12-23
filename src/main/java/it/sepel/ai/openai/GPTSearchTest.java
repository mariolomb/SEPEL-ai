package it.sepel.ai.openai;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* test endpoint con curl
curl https://api.openai.com/v1/chat/completions \
    -H "Authorization: Bearer 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" \
    -H "Content-Type: application/json" \
    -d '{
      "model": "gpt-3.5-turbo",
      "messages": [{"role": "user", "content": "Ciao!"}]
    }'
*/

public class GPTSearchTest {
    
    private final OpenAiService openAiService;
    private static final Logger log = LoggerFactory.getLogger(GPTSearchTest.class);
    
    public GPTSearchTest(String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }
    
    
    private ElasticsearchClient createElastichsearchClient() {
        //crea client per ricerca
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );
        ElasticsearchClient ret = new ElasticsearchClient(transport);
        return ret;
    }

    public String generateGPTResponse(String prompt) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo") // O "gpt-3.5-turbo"
            .messages(List.of(
                new ChatMessage("system", "Sei un assistente intelligente."),
                new ChatMessage("user", prompt)
            ))
            .maxTokens(300)
            .temperature(0.7)
            .build();

        return openAiService.createChatCompletion(request)
            .getChoices().get(0).getMessage().getContent();
    }
    
    public List<String> searchDocuments(String query) {
    List<String> documents = new ArrayList<>();
    ElasticsearchClient elasticsearchClient  = createElastichsearchClient();
    
    try {
        SearchResponse<Map> response = elasticsearchClient.search(s -> s
            .index("sepel_ai")
            .query(q -> q.match(m -> m.field("content").query(query))), Map.class);

        response.hits().hits().forEach(hit -> {
            String content = (String)hit.source().get("content");
            documents.add(content);
        });
    } catch (IOException e) {
        log.error("Errore ricerca elasticsearch", e);
    }

    return documents;
}
    
    public String processQuery(String userQuery) {
        // Step 1: Cerca i documenti rilevanti
        List<String> documents = searchDocuments(userQuery);

        // Step 2: Crea il contesto per il modello GPT
        String context = String.join("\n\n", documents);
        String prompt = String.format("""
            Ecco alcune informazioni rilevanti dai documenti:
            %s

            Domanda: %s
            Risposta:
            """, context, userQuery);

        // Step 3: Genera una risposta con GPT
        return generateGPTResponse(prompt);
    }
    
    public static void main(String[] args) {
        GPTSearchTest t = new GPTSearchTest("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");
        try {
            String userQuery = "per un cittadino comunitario in possesso dei requisiti di cui all'art 7 dlgs 30 è possibili accettare una richiesta di residenza come fosse un lavoratore";
            String queryResult = t.processQuery(userQuery);
            log.info("GPT result = [" + queryResult + "]");
        } catch (Exception e) {
            log.error("Errore", e);
        }
    }
    
}
