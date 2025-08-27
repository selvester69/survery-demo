package com.survery.analytics.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.survery.analytics.transformer.dto.TransformedSurveyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyEventProcessorTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private LocationCacheService locationCacheService;

    @InjectMocks
    private SurveyEventProcessor surveyEventProcessor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        // We can re-initialize the processor to inject a real ObjectMapper if needed,
        // but for this test, direct instantiation is fine as we control the inputs.
    }

    @Test
    void processEvent_shouldTransformAndPublishToTransformedTopic() throws JsonProcessingException {
        // Arrange
        String rawMessage = "{\"survey_id\":\"a8c3e8f7-b0a3-4b1f-9c6e-8d7b3a2d1f0e\",\"location_data\":{\"lat\":12.9,\"lon\":77.5}}";
        UUID villageId = UUID.randomUUID();
        UUID panchayatId = UUID.randomUUID();
        UUID constituencyId = UUID.randomUUID();

        when(locationCacheService.findLocationByCoordinates(12.9, 77.5))
                .thenReturn(new LocationCacheService.LocationData(villageId, panchayatId, constituencyId));

        // Act
        surveyEventProcessor.processEvent(rawMessage);

        // Assert
        verify(kafkaTemplate).send(eq("transformed-surveys"), any(String.class), stringArgumentCaptor.capture());

        String transformedJson = stringArgumentCaptor.getValue();
        TransformedSurveyDto dto = objectMapper.readValue(transformedJson, TransformedSurveyDto.class);

        assertEquals(UUID.fromString("a8c3e8f7-b0a3-4b1f-9c6e-8d7b3a2d1f0e"), dto.getSurveyId());
        assertEquals(villageId, dto.getVillageId());
        assertEquals(panchayatId, dto.getPanchayatId());
        assertEquals(constituencyId, dto.getConstituencyId());
    }

    @Test
    void processEvent_whenProcessingFails_shouldSendToDlq() throws JsonProcessingException {
        // Arrange
        String rawMessage = "{\"invalid_json\": true}";

        // Act
        surveyEventProcessor.processEvent(rawMessage);

        // Assert
        verify(kafkaTemplate).send("dlq-survey-responses", rawMessage);
    }
}
