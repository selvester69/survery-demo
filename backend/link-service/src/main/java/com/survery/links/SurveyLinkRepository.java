package com.survery.links;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurveyLinkRepository extends JpaRepository<SurveyLink, UUID> {

    /**
     * Finds a survey link by its unique, short, public-facing ID.
     *
     * @param linkId The short ID to search for.
     * @return An Optional containing the SurveyLink if found, otherwise empty.
     */
    Optional<SurveyLink> findByLinkId(String linkId);

}
