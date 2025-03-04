package it.sepel.ai.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "sepel_ai") // Nome dell'indice in Elasticsearch
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contenuto {

    @Id
    private String id; 
    private Integer idContenuto;
    private String testo; 
    private String tipoContenuto;
    private String materia;
    private Date data;
    private List<Double> embedding;
    private String embeddingString;
    private String scores;
    private Double score;
    private String link;
    private String titolo;
    private String autore;
    private String stato;
    private Date dataIndicizzazione;
    private Date dataCancellazione;
    private String esitoIndicizzazione;
    private Date dataInserimento;
    private String esito;

    public Contenuto() {
    }

    public String getEsito() {
        return esito;
    }

    public void setEsito(String esito) {
        this.esito = esito;
    }
    
    public String getEmbeddingString() {
        return embeddingString;
    }

    public void setEmbeddingString(String embeddingString) {
        this.embeddingString = embeddingString;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public Date getDataIndicizzazione() {
        return dataIndicizzazione;
    }

    public void setDataIndicizzazione(Date dataIndicizzazione) {
        this.dataIndicizzazione = dataIndicizzazione;
    }

    public Date getDataCancellazione() {
        return dataCancellazione;
    }

    public void setDataCancellazione(Date dataCancellazione) {
        this.dataCancellazione = dataCancellazione;
    }

    public String getEsitoIndicizzazione() {
        return esitoIndicizzazione;
    }

    public void setEsitoIndicizzazione(String esitoIndicizzazione) {
        this.esitoIndicizzazione = esitoIndicizzazione;
    }

    public Date getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(Date dataInserimento) {
        this.dataInserimento = dataInserimento;
    }
    
    public String getAutore() {
        return autore;
    }

    public void setAutore(String autore) {
        this.autore = autore;
    }
    
    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }
    
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getScores() {
        return scores;
    }

    public void setScores(String scores) {
        this.scores = scores;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIdContenuto() {
        return idContenuto;
    }

    public void setIdContenuto(Integer idContenuto) {
        this.idContenuto = idContenuto;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public String getTipoContenuto() {
        return tipoContenuto;
    }

    public void setTipoContenuto(String tipoContenuto) {
        this.tipoContenuto = tipoContenuto;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Contenuto other = (Contenuto) obj;
        return Objects.equals(this.id, other.id);
    }
}

