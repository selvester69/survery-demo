package com.survery.links;

import com.survery.links.dto.LinkResponse;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    // In a real app, this would come from config
    private final String baseUrl = "https://domain.com/s/";

    public LinkResponse toResponse(SurveyLink entity) {
        if (entity == null) {
            return null;
        }

        return LinkResponse.builder()
                .linkId(entity.getLinkId())
                .surveyId(entity.getSurveyId())
                .shortUrl(baseUrl + entity.getLinkId())
                .targetUrl(entity.getTargetUrl())
                .active(entity.isActive())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .clicks(entity.getClicks())
                .build();
    }
}
