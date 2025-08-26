package com.survery.links.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CreateLinkRequest {
    private UUID surveyId;
    private String targetUrl;
    private OffsetDateTime expiresAt;
}
