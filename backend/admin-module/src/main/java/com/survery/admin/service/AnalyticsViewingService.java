package com.survery.admin.service;

import com.survery.admin.dto.analytics.SurveyAnalyticsDTO;
import com.survery.admin.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class AnalyticsViewingService {

    private final WebClient webClient;

    public AnalyticsViewingService(WebClient webClient) {
        this.webClient = webClient;
    }

    public SurveyAnalyticsDTO getAnalyticsForSurvey(UUID surveyId) {
        return webClient.get()
                .uri("/api/v1/analytics/surveys/{surveyId}", surveyId)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.is4xxClientError(),
                        clientResponse -> {
                            if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                                return Mono.error(new ResourceNotFoundException("Analytics data not found for survey ID: " + surveyId));
                            }
                            return Mono.error(new RuntimeException("Client error while fetching analytics data."));
                        })
                .onStatus(httpStatus -> httpStatus.is5xxServerError(),
                        clientResponse -> Mono.error(new RuntimeException("Server error while fetching analytics data.")))
                .bodyToMono(SurveyAnalyticsDTO.class)
                .block(Duration.ofSeconds(10)); // Using .block() to make it synchronous for simplicity in this module
    }
}
