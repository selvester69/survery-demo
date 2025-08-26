package com.survery.analytics.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.survery.analytics.transformer.dto.SurveyEvent;
import com.survery.analytics.transformer.dto.TransformedSurveyDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(SurveyEventProcessor.class);
    private static final String TRANSFORMED_TOPIC = "transformed-surveys";
    private static final String DLQ_TOPIC = "dlq-survey-responses";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final LocationCacheService locationCacheService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(topics = "survey-responses")
    public void processEvent(String message) {
        try {
            // 1. Deserialize JSON
            SurveyEvent rawEvent = objectMapper.readValue(message, SurveyEvent.class);
            log.info("Processing event for surveyId: {}", rawEvent.getSurvey_id());

            // 2. Add validation for the raw event
            if (rawEvent.getSurvey_id() == null || rawEvent.getLocation_data() == null) {
                throw new IllegalArgumentException("Invalid event structure: survey_id or location_data is null.");
            }

            // 3. Enrich with location data from cache
            LocationCacheService.LocationData enrichedLocation = locationCacheService.findLocationByCoordinates(
                rawEvent.getLocation_data().getLat(), rawEvent.getLocation_data().getLon());

            // 4. Create enriched DTO
            TransformedSurveyDto transformedData = new TransformedSurveyDto(
                rawEvent.getSurvey_id(),
                rawEvent.getQuestion_id(),
                rawEvent.getResponse(),
                rawEvent.getUser_id(),
                rawEvent.getTimestamp(),
                rawEvent.getLocation_data().getLat(),
                rawEvent.getLocation_data().getLon(),
                enrichedLocation.getVillageId(),
                enrichedLocation.getPanchayatId(),
                enrichedLocation.getConstituencyId()
            );

            // 4. In a real app, we would add validation logic here.

            // 5. Serialize and publish to a new topic
            String transformedJson = objectMapper.writeValueAsString(transformedData);
            kafkaTemplate.send(TRANSFORMED_TOPIC, rawEvent.getSurvey_id().toString(), transformedJson);
            log.info("Successfully transformed and published event for surveyId: {}", rawEvent.getSurvey_id());

        } catch (Exception e) {
            // 6. Log error and send to Dead-Letter Queue (DLQ)
            log.error("Failed to process event: {}. Sending to DLQ.", message, e);
            kafkaTemplate.send(DLQ_TOPIC, message);
        }
    }
}
