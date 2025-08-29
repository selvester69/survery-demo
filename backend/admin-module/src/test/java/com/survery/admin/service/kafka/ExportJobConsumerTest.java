package com.survery.admin.service.kafka;

import com.survery.admin.model.AdminUser;
import com.survery.admin.model.ExportJob;
import com.survery.admin.model.ExportStatus;
import com.survery.admin.model.Survey;
import com.survery.admin.repository.AdminUserRepository;
import com.survery.admin.repository.ExportJobRepository;
import com.survery.admin.repository.SurveyRepository;
import com.survery.admin.service.s3.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" })
public class ExportJobConsumerTest {

    @Autowired
    private KafkaTemplate<String, UUID> kafkaTemplate;

    @Autowired
    private ExportJobRepository exportJobRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @MockBean
    private S3Service s3Service;

    @Value("${kafka.topic.export-jobs}")
    private String topic;

    @Test
    void whenConsumeExportJob_withSuccessfulUpload_shouldUpdateJobToCompleted() {
        // Arrange
        AdminUser user = adminUserRepository.save(new AdminUser("user@test.com", "pass"));
        Survey survey = surveyRepository.save(new Survey("Test Survey", user));
        ExportJob job = exportJobRepository.save(new ExportJob(survey, user));

        when(s3Service.uploadFile(anyString(), any(byte[].class))).thenReturn("http://s3.com/test-file.csv");

        // Act
        kafkaTemplate.send(topic, job.getId());

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ExportJob finishedJob = exportJobRepository.findById(job.getId()).orElseThrow();
            assertEquals(ExportStatus.COMPLETED, finishedJob.getStatus());
            assertNotNull(finishedJob.getS3Url());
        });
    }

    @Test
    void whenConsumeExportJob_withUploadFailure_shouldUpdateJobToFailed() {
        // Arrange
        AdminUser user = adminUserRepository.save(new AdminUser("user@test.com", "pass"));
        Survey survey = surveyRepository.save(new Survey("Test Survey", user));
        ExportJob job = exportJobRepository.save(new ExportJob(survey, user));

        when(s3Service.uploadFile(anyString(), any(byte[].class))).thenThrow(new RuntimeException("S3 is down"));

        // Act
        kafkaTemplate.send(topic, job.getId());

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ExportJob finishedJob = exportJobRepository.findById(job.getId()).orElseThrow();
            assertEquals(ExportStatus.FAILED, finishedJob.getStatus());
        });
    }
}
