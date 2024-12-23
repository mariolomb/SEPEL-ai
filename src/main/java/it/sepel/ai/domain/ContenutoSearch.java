package it.sepel.ai.domain;

public class ContenutoSearch {
    
    private Integer idContenuto;
    private String tipoContenuto;
    private String id;
    private String embeddingIsEmpty;
    private String errori;

    public String getErrori() {
        return errori;
    }

    public void setErrori(String errori) {
        this.errori = errori;
    }

    public String getEmbeddingIsEmpty() {
        return embeddingIsEmpty;
    }

    public void setEmbeddingIsEmpty(String embeddingIsEmpty) {
        this.embeddingIsEmpty = embeddingIsEmpty;
    }
    
    public Integer getIdContenuto() {
        return idContenuto;
    }

    public void setIdContenuto(Integer idContenuto) {
        this.idContenuto = idContenuto;
    }

    public String getTipoContenuto() {
        return tipoContenuto;
    }

    public void setTipoContenuto(String tipoContenuto) {
        this.tipoContenuto = tipoContenuto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}
