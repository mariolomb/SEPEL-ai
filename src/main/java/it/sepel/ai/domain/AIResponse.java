package it.sepel.ai.domain;

public class AIResponse {
    
    private String id;
    private String prompt;
    private String response;
    private Long promptTokens;
    private Long completionTokens;
    private Long totalTokens;
    private Integer idAbbonato;
    private String codice;
    private String chiarimento;
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getChiarimento() {
        return chiarimento;
    }

    public void setChiarimento(String chiarimento) {
        this.chiarimento = chiarimento;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIdAbbonato() {
        return idAbbonato;
    }

    public void setIdAbbonato(Integer idAbbonato) {
        this.idAbbonato = idAbbonato;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Long getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Long completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Long totalTokens) {
        this.totalTokens = totalTokens;
    }
}
