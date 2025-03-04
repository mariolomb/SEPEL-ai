package it.sepel.ai.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TutorChatMessage implements Serializable {
    private String id;
    private String text;
    private String role;
    private Date date;
    private List<Contenuto> attachments;

    public TutorChatMessage(String role, String text) {
       this.date = new Date();
       this.role = role;
       this.text = text;
       this.attachments = new ArrayList<>();
    }
    
    public TutorChatMessage(String role, AIResponse r) {
       this.id = r.getId();
       this.date = new Date();
       this.role = role;
       this.text = r.getResponse();
       this.attachments = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Contenuto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Contenuto> attachments) {
        this.attachments = attachments;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getText() {
        return text;
    }

    public String getRole() {
        return role;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
