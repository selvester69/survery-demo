package com.survery.admin.service;

import com.survery.admin.dto.link.CreateLinkRequestDTO;
import com.survery.admin.dto.link.LinkResponseDTO;
import com.survery.admin.exception.ResourceNotFoundException;
import com.survery.admin.repository.SurveyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class LinksOrchestrationService {

    private final WebClient linkServiceWebClient;
    private final SurveyRepository surveyRepository;

    public LinksOrchestrationService(@Qualifier("linkServiceWebClient") WebClient linkServiceWebClient,
                                     SurveyRepository surveyRepository) {
        this.linkServiceWebClient = linkServiceWebClient;
        this.surveyRepository = surveyRepository;
    }

    public LinkResponseDTO createLinkForSurvey(UUID surveyId) {
        // 1. Verify survey exists
        if (!surveyRepository.existsById(surveyId)) {
            throw new ResourceNotFoundException("Survey not found with ID: " + surveyId);
        }

        // 2. Construct the long URL
        // In a real application, this base URL would be in application.properties
        String longUrl = "http://public-facing-app.com/surveys/" + surveyId.toString();
        CreateLinkRequestDTO requestDTO = new CreateLinkRequestDTO();
        requestDTO.setLongUrl(longUrl);

        // 3. Call the link-service
        return linkServiceWebClient.post()
                .uri("/api/v1/links")
                .body(Mono.just(requestDTO), CreateLinkRequestDTO.class)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.isError(),
                        clientResponse -> Mono.error(new RuntimeException("Failed to create link. Status: " + clientResponse.statusCode())))
                .bodyToMono(LinkResponseDTO.class)
                .block(Duration.ofSeconds(10));
    }
}
