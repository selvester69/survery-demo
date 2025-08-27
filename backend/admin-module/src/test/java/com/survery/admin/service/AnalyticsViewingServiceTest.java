package com.survery.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survery.admin.dto.analytics.SurveyAnalyticsDTO;
import com.survery.admin.exception.ResourceNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsViewingServiceTest {

    private static MockWebServer mockWebServer;
    private AnalyticsViewingService analyticsViewingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        analyticsViewingService = new AnalyticsViewingService(webClient);
    }

    @Test
    void whenGetAnalytics_withExistingSurvey_shouldReturnDto() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        SurveyAnalyticsDTO mockDto = new SurveyAnalyticsDTO();
        mockDto.setSurveyId(surveyId);
        mockDto.setSurveyTitle("Test Analytics");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockDto))
                .addHeader("Content-Type", "application/json"));

        // Act
        SurveyAnalyticsDTO result = analyticsViewingService.getAnalyticsForSurvey(surveyId);

        // Assert
        assertNotNull(result);
        assertEquals(surveyId, result.getSurveyId());
        assertEquals("Test Analytics", result.getSurveyTitle());
    }

    @Test
    void whenGetAnalytics_withNonExistentSurvey_shouldThrowResourceNotFoundException() {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            analyticsViewingService.getAnalyticsForSurvey(surveyId);
        });
    }

    @Test
    void whenGetAnalytics_withServerError_shouldThrowRuntimeException() {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            analyticsViewingService.getAnalyticsForSurvey(surveyId);
        });
    }
}
