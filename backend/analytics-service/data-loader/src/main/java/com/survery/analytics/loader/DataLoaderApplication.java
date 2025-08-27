package com.survery.analytics.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.survery.analytics.domain") // Scan for entities in the domain module
public class DataLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataLoaderApplication.class, args);
    }

}
