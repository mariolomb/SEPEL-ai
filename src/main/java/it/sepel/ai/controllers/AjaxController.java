package it.sepel.ai.controllers;

import it.sepel.acl2sb.domain.LoginResult;
import it.sepel.ai.AppException;
import it.sepel.ai.Response;
import it.sepel.ai.domain.ELSearch;
import it.sepel.ai.domain.ItemCronologia;
import it.sepel.ai.domain.Rating;
import it.sepel.ai.domain.TutorChatSession;
import it.sepel.ai.elasticsearch.Indexer;
import it.sepel.ai.logic.Manager;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
    
    @PostMapping(value = "updateRating")
    public ResponseEntity<Object> updateRating(@RequestBody Rating r) {
        Response resp;
        try {
            manager.updateRating(r);
            resp = new Response("OK");
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
            Subject currentUser = SecurityUtils.getSubject();
            LoginResult rs = (LoginResult) currentUser.getPrincipal();
            Session session = SecurityUtils.getSubject().getSession();
            session.setAttribute("chatSession", new TutorChatSession(rs.getCodice()));
            resp = new Response("OK");
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    /*
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
    */
    
    /*
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
    */
    
    @PostMapping(value = "checkContenuti")
    public ResponseEntity<Object> checkContenuti() {
        Response resp;
        try {
            indexer.checkContenuti();
            resp = new Response("OK");
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    //torna direttamente la stringa
    @GetMapping(value = "getEmbeddingString")
    public ResponseEntity<String> getEmbeddingString(@RequestParam(name = "searchTerm") String searchTerm) {
        try {
            String embeddingString = indexer.getEmbeddingString(searchTerm);
            return ResponseEntity.ok(embeddingString);
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity.internalServerError().body("Errore durante il recupero dell'embedding");
        }
    }
    
    @PostMapping(value = "getCronologia")
    public ResponseEntity<Object> getCronologia() {
        Response resp;
        try {
            Subject currentUser = SecurityUtils.getSubject();
            LoginResult rs = (LoginResult) currentUser.getPrincipal();
            List<ItemCronologia> cc = manager.loadCronologia(rs.getCodice());
            resp = new Response(cc);
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
    
    @PostMapping(value = "loadMaterie")
    public ResponseEntity<Object> loadMaterie() {
        Response resp;
        try {
            List<String> mm = manager.loadMaterie();
            resp = new Response(mm);
        } catch (Exception e) {
            resp = new Response(e);
            log.error("Error", e);
        }
        return new ResponseEntity<>(resp, resp.getHttpStatus());
    }
}
