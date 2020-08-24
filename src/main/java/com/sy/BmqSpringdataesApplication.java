package com.sy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.sy.es.repository")
public class BmqSpringdataesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmqSpringdataesApplication.class, args);
    }

}
