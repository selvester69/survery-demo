package com.survery.admin.controller;

import com.survery.admin.model.AdminUser;
import com.survery.admin.model.ExportJob;
import com.survery.admin.model.Survey;
import com.survery.admin.repository.ExportJobRepository;
import com.survery.admin.repository.SurveyRepository;
import com.survery.admin.repository.AdminUserRepository;
import com.survery.admin.service.kafka.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ExportJobRepository exportJobRepository;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @Test
    @WithMockUser(username = "test@example.com")
    void whenRequestSurveyExport_shouldCreateJobAndSendMessage() throws Exception {
        // Arrange
        AdminUser user = new AdminUser();
        user.setEmail("test@example.com");
        user.setPassword("password");
        adminUserRepository.save(user);

        Survey survey = new Survey();
        survey.setTitle("Test Survey for Export");
        survey.setCreatedBy(user);
        surveyRepository.save(survey);

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/surveys/{surveyId}/export", survey.getId()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.message").value("Export job created successfully."));

        // Verify a job was created
        ExportJob job = exportJobRepository.findAll().get(0);
        assert job.getSurvey().getId().equals(survey.getId());

        // Verify Kafka message was sent
        verify(kafkaProducerService).sendExportJobRequest(job.getId());
    }
}
