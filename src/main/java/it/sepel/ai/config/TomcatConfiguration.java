package it.sepel.ai.config;

import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TomcatConfiguration {
    
    @Bean
    @Profile("prod")
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return factory -> {
            factory.setPort(8017); // Imposta la porta del server Tomcat su 8004
            factory.setProtocol("AJP/1.3"); // Imposta il protocollo AJP
            factory.addConnectorCustomizers(connector -> {
                AjpNioProtocol protocol = (AjpNioProtocol) connector.getProtocolHandler();
                protocol.setSecretRequired(false); // Imposta la richiesta di segreto AJP a false
            });
        };
    }
    
    @Bean
    @Profile("test")
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.setPort(8080);
        return tomcat;
    }
}