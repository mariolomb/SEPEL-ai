package it.sepel.ai.config;

import freemarker.template.TemplateException;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Configuration
public class FreemarkerConfiguration {
    
    @Bean
    @Primary
    public FreeMarkerConfigurationFactoryBean freeMarkerConfigurationBean() {
        FreeMarkerConfigurationFactoryBean config = new FreeMarkerConfigurationFactoryBean();
        config.setTemplateLoaderPaths("classpath:/templates/"); // path dei template
        config.setDefaultEncoding("UTF-8"); // encoding di default dei template
        /*
        Map<String, Object> variables = new HashMap<>();
        variables.put("authenticated", new AuthenticatedDirective());
        variables.put("notAuthenticated", new NotAuthenticatedDirective()); 
        variables.put("hasRole", new HasRoleDirective()); 
        config.setFreemarkerVariables(variables);
        */
        return config;
    }
    
     @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer(FreeMarkerConfigurationFactoryBean freeMarkerConfiguration) throws IOException, TemplateException {
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(freeMarkerConfiguration.createConfiguration());
        return freeMarkerConfigurer;
    }
}
