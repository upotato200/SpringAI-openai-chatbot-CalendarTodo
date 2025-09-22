package com.best.caltodocrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.best.caltodocrud.infrastructure.persistence.jpa")
@EnableJpaRepositories(basePackages = "com.best.caltodocrud.infrastructure.persistence.jpa")
@EnableJpaAuditing
public class CalTodoCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalTodoCrudApplication.class, args);
    }

}
