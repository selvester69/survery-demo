package com.survey.service.repository;

import com.survey.service.model.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    List<Survey> findByCreatedBy(UUID ownerId);

    List<Survey> findByStatusAndExpiresAtAfter(String status, LocalDateTime date);
}
