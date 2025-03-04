package it.sepel.ai.domain;

import java.util.Date;

public class ItemCronologia {
    
    private String prompt;
    private Date data;
    private String codiceAbbonato;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getCodiceAbbonato() {
        return codiceAbbonato;
    }

    public void setCodiceAbbonato(String codiceAbbonato) {
        this.codiceAbbonato = codiceAbbonato;
    }
}
