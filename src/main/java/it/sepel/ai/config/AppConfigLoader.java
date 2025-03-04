package it.sepel.ai.config;

import org.springframework.boot.ApplicationRunner;
import it.sepel.ai.mapper.ApplicationMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigLoader {
    
    @Autowired
    private ApplicationMapper mapper;
    
    private static final Logger log = LoggerFactory.getLogger(AppConfigLoader.class);
    
    @Bean
    public ApplicationRunner loadAppConfig() {
        return args -> {
            String apiKey = mapper.loadConfigProperty("OPENAI_API_KEY");
            if (StringUtils.isNoneEmpty(apiKey)) {
                log.info("OPENAI_API_KEY caricata: " + apiKey);
                System.setProperty("OPENAI_API_KEY", apiKey);
            } else {
                log.info("OPENAI_API_KEY non trovata nel database.");
            }
        };
    }
    
}
