package it.sepel.ai.domain;

import java.util.List;

public class ELSearch {
    
    private String searchTerm;
    private List<Double> searchTermEmbedding;

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
