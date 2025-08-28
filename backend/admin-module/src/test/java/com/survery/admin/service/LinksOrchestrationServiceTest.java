package com.survery.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survery.admin.dto.link.LinkResponseDTO;
import com.survery.admin.exception.ResourceNotFoundException;
import com.survery.admin.repository.SurveyRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LinksOrchestrationServiceTest {

    private static MockWebServer mockWebServer;
    private LinksOrchestrationService linksOrchestrationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SurveyRepository surveyRepository;

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
        linksOrchestrationService = new LinksOrchestrationService(webClient, surveyRepository);
    }

    @Test
    void whenCreateLink_withExistingSurvey_shouldSucceed() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        when(surveyRepository.existsById(surveyId)).thenReturn(true);

        LinkResponseDTO mockResponse = new LinkResponseDTO();
        mockResponse.setId("shortId");
        mockResponse.setShortUrl(mockWebServer.url("/l/shortId").toString());
        mockResponse.setLongUrl("http://public-facing-app.com/surveys/" + surveyId);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader("Content-Type", "application/json"));

        // Act
        LinkResponseDTO result = linksOrchestrationService.createLinkForSurvey(surveyId);

        // Assert
        assertNotNull(result);
        assertEquals("shortId", result.getId());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/api/v1/links", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
        assertTrue(recordedRequest.getBody().readUtf8().contains(surveyId.toString()));
    }

    @Test
    void whenCreateLink_withNonExistentSurvey_shouldThrowResourceNotFoundException() {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        when(surveyRepository.existsById(surveyId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            linksOrchestrationService.createLinkForSurvey(surveyId);
        });
    }

    @Test
    void whenCreateLink_andLinkServiceFails_shouldThrowRuntimeException() {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        when(surveyRepository.existsById(surveyId)).thenReturn(true);
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            linksOrchestrationService.createLinkForSurvey(surveyId);
        });
    }
}
