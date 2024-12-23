package it.sepel.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableElasticsearchRepositories
@SpringBootApplication
@MapperScan({"it.sepel.ai.mapper", "it.sepel.acl2sb.mapper"})
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
