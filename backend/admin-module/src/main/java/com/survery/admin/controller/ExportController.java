package com.survery.admin.controller;

import com.survery.admin.dto.export.ExportResponseDTO;
import com.survery.admin.exception.ResourceNotFoundException;
import com.survery.admin.model.AdminUser;
import com.survery.admin.model.ExportJob;
import com.survery.admin.model.Survey;
import com.survery.admin.repository.ExportJobRepository;
import com.survery.admin.repository.SurveyRepository;
import com.survery.admin.service.kafka.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/surveys/{surveyId}/export")
public class ExportController {

    private final SurveyRepository surveyRepository;
    private final ExportJobRepository exportJobRepository;
    private final KafkaProducerService kafkaProducerService;

    public ExportController(SurveyRepository surveyRepository,
                              ExportJobRepository exportJobRepository,
                              KafkaProducerService kafkaProducerService) {
        this.surveyRepository = surveyRepository;
        this.exportJobRepository = exportJobRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ExportResponseDTO> requestSurveyExport(
            @PathVariable UUID surveyId,
            @AuthenticationPrincipal AdminUser currentUser) {

        // 1. Verify the survey exists
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + surveyId));

        // 2. Create and save the job record
        ExportJob newJob = new ExportJob();
        newJob.setSurvey(survey);
        newJob.setCreatedBy(currentUser);
        ExportJob savedJob = exportJobRepository.save(newJob);

        // 3. Send the job request to Kafka
        kafkaProducerService.sendExportJobRequest(savedJob.getId());

        // 4. Return the job ID to the client
        ExportResponseDTO response = new ExportResponseDTO(savedJob.getId(), "Export job created successfully.");
        return ResponseEntity.accepted().body(response);
    }
}
