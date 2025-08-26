package com.survery.analytics.transformer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyEvent {
    private UUID survey_id;
    private String question_id;
    private Map<String, Object> response;
    private Location location_data;
    private UUID user_id;
    private OffsetDateTime timestamp;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private double lat;
        private double lon; // HLD used 'long', but 'lon' is more common and less likely to conflict with keywords
    }
}
