package it.sepel.ai.domain;

import it.sepel.ai.logic.Manager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TutorChatSession implements Serializable {
    private Date startDate;
    private List<TutorChatMessage> messages;

    public TutorChatSession() {
        this.startDate = new Date();
        this.messages = new ArrayList();
        TutorChatMessage m = new TutorChatMessage();
        m.setText("Ciao come posso aiutarti oggi?");
        m.setDate(new Date());
        m.setId(UUID.randomUUID().toString());
        m.setRole(Manager.CHAT_ROLE_TUTOR);
        this.messages.add(m);
    }
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public List<TutorChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TutorChatMessage> messages) {
        this.messages = messages;
    }
}
