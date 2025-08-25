package com.survery.links.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class LinkResponse {
    private String linkId;
    private UUID surveyId;
    private String shortUrl;
    private String targetUrl;
    private boolean active;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private long clicks;
}
