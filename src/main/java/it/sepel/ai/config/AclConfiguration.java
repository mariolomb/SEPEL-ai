package it.sepel.ai.config;

import it.sepel.acl2sb.logic.AclManager;
import it.sepel.acl2sb.logic.AclRealm;
import it.sepel.acl2sb.logic.JwtManager;
import it.sepel.acl2sb.logic.LoginResultLoader;
import it.sepel.acl2sb.logic.SimpleLoginResultLoader;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AclConfiguration {

    static final public String JWTKEY_FILE = "/opt/applicazioni/SEPEL-ai/sessioni/jwtkey.ser";

    @Bean
    public Realm realm() {
        return new AclRealm();
    }

    @Bean
    public AclManager aclManager() {
        return new AclManager();
    }

    //login result loader di tipo simple. prende i dati dal jwt token. da usare con parametri username e password
    /*
    @Bean
    public LoginResultLoader loginResultLoader() {
        return new DbLoginResultLoader();
    }
    */
    
    //login result loader da usare per il login con parametro ssnId
    @Bean
    public LoginResultLoader loginResultLoader() {
        return new SimpleLoginResultLoader();
    }

    @Bean
    public JwtManager jwtManager() {
        JwtManager ret = new JwtManager();
        ret.startUp(JWTKEY_FILE);
        return new JwtManager();
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/**", "anon"); // all paths are managed via annotations

        // or allow basic authentication, but NOT require it.
        // chainDefinition.addPathDefinition("/**", "authcBasic[permissive]");
        return chainDefinition;
    }
    
    /*
    @Bean
    public DefaultSecurityManager securityManager(Realm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        return securityManager;
    }

    @Bean
    public Authorizer authorizer(DefaultSecurityManager securityManager) {
        return securityManager;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/**", "anon"); // all paths are managed via annotations
        return chainDefinition;
    }
    
    @Bean
    public EventBus eventBus() {
        return new DefaultEventBus();
    }
*/

}
