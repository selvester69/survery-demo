package com.survey.response.repository;

import com.survey.response.entity.SurveyResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, Long> {
    // Custom query methods can be added here if needed
}
