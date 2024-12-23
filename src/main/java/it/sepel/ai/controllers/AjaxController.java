package it.sepel.ai.controllers;

import it.sepel.ai.AppException;
import it.sepel.ai.Response;
import it.sepel.ai.domain.Contenuto;
import it.sepel.ai.domain.ELSearch;
import it.sepel.ai.domain.TutorChatMessage;
import it.sepel.ai.domain.TutorChatSession;
import it.sepel.ai.elasticsearch.Indexer;
import it.sepel.ai.logic.Manager;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AjaxController {


    private static final Logger log = LoggerFactory.getLogger(AjaxController.class);
    
    
    @Autowired
    Manager manager;
    
    @Autowired
    Indexer indexer;

    //gestisce le eccezioni runtime tipo ruolo non valido
    @ExceptionHandler({Exception.class})
    public ResponseEntity handleException(Exception ex) throws Exception {
        /*
        if (ex instanceof UnauthenticatedException) {
            ex = new AppException("Sessione scaduta. Rifare il login");
        }
         */
        log.error("ERRORE", ex);
        if (ex instanceof org.springframework.validation.BindException) {
            final StringBuilder sb = new StringBuilder();
            BindException be = (BindException) ex;
            be.getBindingResult().getAllErrors().forEach((error) -> {
                //String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                sb.append(errorMessage + "<br />");
            });
            ex = new AppException(sb.toString());
        }

        Response resp = new Response(ex);
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    @PostMapping(value = "processTutorQuery")
    public ResponseEntity<Object> processTutorQuery(@RequestBody ELSearch s) {
        Response resp;
        try {
            if(StringUtils.isEmpty(s.getSearchTerm())) {
                throw new AppException("La domanda non può essere vuota");
            }
            Session session = SecurityUtils.getSubject().getSession();
            TutorChatSession cs = (TutorChatSession)session.getAttribute("chatSession");
            manager.processTutorQuery(s, cs);
            resp = new Response(cs);
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    @PostMapping(value = "getChatSession")
    public ResponseEntity<Object> getChatSession() {
        Response resp;
        try {
            Session session = SecurityUtils.getSubject().getSession();
            TutorChatSession cs = (TutorChatSession)session.getAttribute("chatSession");
            resp = new Response(cs);
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    @PostMapping(value = "resetChat")
    public ResponseEntity<Object> resetChat() {
        Response resp;
        try {
            Session session = SecurityUtils.getSubject().getSession();
            session.setAttribute("chatSession", new TutorChatSession());
            resp = new Response("OK");
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    @PostMapping(value = "indexAllContenutiFromFiles")
    public ResponseEntity<Object> indexAllContenutiFromFiles() {
        Response resp;
        try {
            indexer.indexAllContenutiFromFiles();
            resp = new Response("OK");
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    @PostMapping(value = "indexOneContenutoFromFile")
    public ResponseEntity<Object> indexOneContenutoFromFile() {
        Response resp;
        try {
            indexer.indexOneContenutoFromFile();
            resp = new Response("OK");
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    @PostMapping(value = "indexAllContenutiFromDb")
    public ResponseEntity<Object> indexAllContenutiFromDb() {
        Response resp;
        try {
            indexer.indexAllContenutiFromDb();
            resp = new Response("OK");
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
}
