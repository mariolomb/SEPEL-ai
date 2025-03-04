package it.sepel.ai.domain;

public class Rating {
    private String id;
    private Integer ratingValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }
}
