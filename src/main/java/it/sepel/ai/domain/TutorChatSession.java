package it.sepel.ai.domain;

import it.sepel.ai.logic.Manager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TutorChatSession implements Serializable {

    private String id;
    private Date startDate;
    private List<TutorChatMessage> messages;

    public TutorChatSession(String username) {
        this.id = UUID.randomUUID().toString();
        this.startDate = new Date();
        this.messages = new ArrayList();
        TutorChatMessage m = new TutorChatMessage(Manager.SYSTEM_ROLE, "Ciao " + username + " come posso aiutarti oggi?");
        addMessage(m);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void addMessage(TutorChatMessage msg) {
        this.messages.add(msg);

        //ordina per data decrescente
        messages.sort((m1, m2) -> m1.getDate().compareTo(m2.getDate()));
    }

    public TutorChatMessage getLastUserMessage() {
        return this.messages.stream()
                .filter(msg -> Manager.USER_ROLE.equals(msg.getRole())) // Filtra solo i messaggi con ruolo "user"
                .max(Comparator.comparing(TutorChatMessage::getDate)) // Trova il messaggio più recente
                .orElse(null); // Restituisci null se non ci sono messaggi con ruolo "user"
    }
    
    public TutorChatMessage getLastSystemMessage() {
        return this.messages.stream()
                .filter(msg -> Manager.SYSTEM_ROLE.equals(msg.getRole())) // Filtra solo i messaggi con ruolo "user"
                .max(Comparator.comparing(TutorChatMessage::getDate)) // Trova il messaggio più recente
                .orElse(null); // Restituisci null se non ci sono messaggi con ruolo "user"
    }
}
