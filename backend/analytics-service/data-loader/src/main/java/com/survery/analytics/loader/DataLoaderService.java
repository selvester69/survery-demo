package com.survery.analytics.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.survery.analytics.loader.dto.EnrichedSurveyDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private static final Logger log = LoggerFactory.getLogger(DataLoaderService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // In a real implementation, we would inject a repository or a JDBC template
    // to perform batch upserts into the database.

    @KafkaListener(topics = "transformed-surveys")
    public void consumeEnrichedSurvey(String message) {
        try {
            EnrichedSurveyDto dto = objectMapper.readValue(message, EnrichedSurveyDto.class);
            log.info("Received enriched survey for surveyId: {}. Ready to be loaded into the database.", dto.getSurveyId());

            // Database loading logic would go here.
            // For now, we just log that we received it.

        } catch (Exception e) {
            log.error("Failed to process enriched survey event: {}", message, e);
            // In a real app, this would also go to a DLQ.
        }
    }
}
