package com.survery.analytics.api.repository;

import com.survery.analytics.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    // Basic CRUD methods, including count(), are provided by JpaRepository.
}
