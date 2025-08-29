package com.survery.admin.controller;

import com.survery.admin.dto.link.LinkResponseDTO;
import com.survery.admin.service.LinksOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/surveys/{surveyId}/links")
public class LinksOrchestrationController {

    private final LinksOrchestrationService linksOrchestrationService;

    public LinksOrchestrationController(LinksOrchestrationService linksOrchestrationService) {
        this.linksOrchestrationService = linksOrchestrationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LinkResponseDTO> createLinkForSurvey(@PathVariable UUID surveyId) {
        LinkResponseDTO response = linksOrchestrationService.createLinkForSurvey(surveyId);
        return ResponseEntity.ok(response);
    }
}
