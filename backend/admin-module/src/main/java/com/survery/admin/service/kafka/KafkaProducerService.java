package com.survery.admin.service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);

    @Value("${kafka.topic.export-jobs}")
    private String exportJobsTopic;

    private final KafkaTemplate<String, UUID> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, UUID> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendExportJobRequest(UUID jobId) {
        LOGGER.info("Sending export job request for job ID: {}", jobId);
        kafkaTemplate.send(exportJobsTopic, jobId);
    }
}
