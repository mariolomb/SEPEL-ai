package it.sepel.ai.config;

import it.sepel.ai.filter.IpWhiteListFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean<IpWhiteListFilter> loggingFilter() {
        FilterRegistrationBean<IpWhiteListFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new IpWhiteListFilter());
        registrationBean.addUrlPatterns("/getEmbeddingString"); // Filtra SOLO questa rotta
        registrationBean.setOrder(1); // Imposta la priorità del filtro
        return registrationBean;
    }

}
