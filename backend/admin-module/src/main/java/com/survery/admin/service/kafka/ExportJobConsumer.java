package com.survery.admin.service.kafka;

import com.survery.admin.model.ExportJob;
import com.survery.admin.model.ExportStatus;
import com.survery.admin.repository.ExportJobRepository;
import com.survery.admin.service.s3.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ExportJobConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportJobConsumer.class);

    private final ExportJobRepository exportJobRepository;
    private final S3Service s3Service;

    public ExportJobConsumer(ExportJobRepository exportJobRepository, S3Service s3Service) {
        this.exportJobRepository = exportJobRepository;
        this.s3Service = s3Service;
    }

    @KafkaListener(topics = "${kafka.topic.export-jobs}", groupId = "admin-module-group")
    @Transactional
    public void consumeExportJobRequest(UUID jobId) {
        LOGGER.info("Received export job request for job ID: {}", jobId);

        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));

        try {
            // 1. Update status to PROCESSING
            job.setStatus(ExportStatus.PROCESSING);
            exportJobRepository.save(job);

            // 2. Fetch data and generate file (dummy data for now)
            LOGGER.info("Generating export data for survey: {}", job.getSurvey().getTitle());
            String csvData = "survey_id,survey_title,job_id\n" +
                             job.getSurvey().getId() + "," +
                             job.getSurvey().getTitle() + "," +
                             job.getId();
            byte[] fileBytes = csvData.getBytes(StandardCharsets.UTF_8);

            // Simulate some work
            Thread.sleep(5000);

            // 3. Upload to S3
            String fileKey = "exports/" + job.getSurvey().getId() + "/" + job.getId() + ".csv";
            String s3Url = s3Service.uploadFile(fileKey, fileBytes);

            // 4. Update status to COMPLETED
            job.setStatus(ExportStatus.COMPLETED);
            job.setS3Url(s3Url);
            job.setCompletedAt(OffsetDateTime.now());
            exportJobRepository.save(job);
            LOGGER.info("Export job {} completed successfully. File available at: {}", jobId, s3Url);

        } catch (Exception e) {
            LOGGER.error("Failed to process export job {}", jobId, e);
            // 5. Update status to FAILED
            job.setStatus(ExportStatus.FAILED);
            job.setCompletedAt(OffsetDateTime.now());
            exportJobRepository.save(job);
        }
    }
}
