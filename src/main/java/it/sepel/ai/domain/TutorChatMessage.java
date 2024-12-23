package it.sepel.ai.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TutorChatMessage implements Serializable {
    private String id;
    private String text;
    private String role;
    private Date date;
    private List<Contenuto> attachments;

    public TutorChatMessage() {
       this.id = UUID.randomUUID().toString();
       this.date = new Date();
       this.attachments = new ArrayList<>();
    }
    
    public TutorChatMessage(String role, String text) {
       this.id = UUID.randomUUID().toString();
       this.date = new Date();
       this.role = role;
       this.text = text;
       this.attachments = new ArrayList<>();
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
    
    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getRole() {
        return role;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
