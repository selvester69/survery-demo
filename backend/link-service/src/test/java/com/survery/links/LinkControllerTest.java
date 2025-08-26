package com.survery.links;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survery.links.dto.CreateLinkRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Roll back transactions after each test
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createLink_whenValidRequest_shouldReturn201AndLinkResponse() throws Exception {
        // Arrange
        CreateLinkRequest request = new CreateLinkRequest();
        request.setSurveyId(UUID.randomUUID());
        request.setTargetUrl("https://example.com/survey/new-survey");

        // Act & Assert
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.linkId").exists())
                .andExpect(jsonPath("$.linkId").isString())
                .andExpect(jsonPath("$.surveyId").value(request.getSurveyId().toString()))
                .andExpect(jsonPath("$.targetUrl").value(request.getTargetUrl()))
                .andExpect(jsonPath("$.active").value(true));
    }
}
