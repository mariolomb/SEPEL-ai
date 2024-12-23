package it.sepel.ai.controllers;

import it.sepel.acl2sb.domain.LoginResult;
import it.sepel.acl2sb.logic.AclManager;
import it.sepel.ai.domain.TutorChatSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @Autowired
    AclManager aclManager;

    @ExceptionHandler({Exception.class})
    public ModelAndView handleException(Exception ex, Model model, HttpServletRequest req) {
        log.info("Exception [" + ex.getMessage() + "]");
        ModelAndView ret;
        //if (ex instanceof UnauthenticatedException || ex instanceof org.jose4j.jwt.consumer.InvalidJwtException) {
        //    ret = new ModelAndView("redirect:https://www.sepel.it/area-riservata");
        //} else {
        //model.addAttribute("loggedUser", SecurityUtils.getSubject().getPrincipal());
        model.addAttribute("includePage", "500.ftlh");
        model.addAttribute("errorMessage", ex.getMessage());
        Map m = new HashMap();
        m.put("model", model);
        ret = new ModelAndView("index", m);
        //}
        return ret;
    }

    @GetMapping(value = "LOGIN.do")
    public ModelAndView LOGIN(@RequestParam("ssnId") String ssnId, @ModelAttribute("model") ModelMap model, HttpSession ssn) throws Exception {
        ModelAndView ret;
        try {
            log.info("Inizio login");
            Subject currentUser = SecurityUtils.getSubject();
            if(!currentUser.isAuthenticated()) {
                aclManager.login(ssnId);
                
                //genero una chat di messaggi e la associo alla sessione utente
                Session session = SecurityUtils.getSubject().getSession();
                TutorChatSession cs = new TutorChatSession();
                session.setAttribute("chatSession", cs);
            } 
            model.addAttribute("loggedUser", currentUser.getPrincipal());
            LoginResult rs = (LoginResult) currentUser.getPrincipal();
            model.addAttribute("loggedUser", rs);
            model.addAttribute("includePage", "main.ftlh");
            ret = new ModelAndView("index", model);
            log.info("Eseguito login");
        } catch (Exception ex) {
            log.error("", ex);
            model.addAttribute("messaggioErrore", ex.getMessage());
            throw ex;
        }
        return ret;
    }

    @GetMapping(value = "LOGOUT.do")
    public ModelAndView LOGOUT(HttpSession ssn) throws Exception {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();
        log.debug("Eseguito logout");
        return new ModelAndView("redirect:" + "https://www.sepel.it/area-riservata");
    }

    @RequiresAuthentication
    @GetMapping(value = "/")
    public ModelAndView home(@ModelAttribute("model") ModelMap model, HttpSession ssn) throws Exception {
        ModelAndView ret;
        
        Subject currentUser = SecurityUtils.getSubject();
        log.info("User is logged = [" + currentUser.isAuthenticated() + "]");
        
        /*
        model.addAttribute("loggedUser", currentUser.getPrincipal());
        if (currentUser.hasRole("QUESITI_OPERATOR")) {
            model.addAttribute("includePage", "main.ftlh");
            ret = new ModelAndView("index", model);
        } else {
            ret = new ModelAndView("redirect:" + "https://www.sepel.it/area-riservata");
        }
         */
        model.addAttribute("includePage", "main.ftlh");
        ret = new ModelAndView("index", model);
        return ret;
    }
}
