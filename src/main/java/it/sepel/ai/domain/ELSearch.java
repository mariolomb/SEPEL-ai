package it.sepel.ai.domain;

import java.util.List;

public class ELSearch {
    
    private String searchTerm;
    private List<Double> searchTermEmbedding;
    private List<String> requestedMatters;

    public List<String> getRequestedMatters() {
        return requestedMatters;
    }

    public void setRequestedMatters(List<String> requestedMatters) {
        this.requestedMatters = requestedMatters;
    }
    
    public List<Double> getSearchTermEmbedding() {
        return searchTermEmbedding;
    }

    public void setSearchTermEmbedding(List<Double> searchTermEmbedding) {
        this.searchTermEmbedding = searchTermEmbedding;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searcTerm) {
        this.searchTerm = searcTerm;
    }
}
