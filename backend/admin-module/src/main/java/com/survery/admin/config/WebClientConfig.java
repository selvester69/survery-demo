package com.survery.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${analytics.service.base-url}")
    private String analyticsServiceBaseUrl;

    @Value("${link.service.base-url}")
    private String linkServiceBaseUrl;

    @Bean
    @Qualifier("analyticsWebClient")
    public WebClient analyticsWebClient() {
        return WebClient.builder()
                .baseUrl(analyticsServiceBaseUrl)
                .build();
    }

    @Bean
    @Qualifier("linkServiceWebClient")
    public WebClient linkServiceWebClient() {
        return WebClient.builder()
                .baseUrl(linkServiceBaseUrl)
                .build();
    }
}
