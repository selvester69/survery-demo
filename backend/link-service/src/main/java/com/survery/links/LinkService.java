package com.survery.links;

import com.survery.links.dto.CreateLinkRequest;
import com.survery.links.dto.LinkResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final SurveyLinkRepository surveyLinkRepository;
    private final LinkMapper linkMapper;

    @Transactional
    public LinkResponse createLink(CreateLinkRequest request) {
        // HLD requires collision handling, but for now we'll do a single generation.
        // A more robust implementation would loop or use a different strategy.
        String linkId = IdGenerator.generate(8);

        SurveyLink surveyLink = SurveyLink.builder()
                .linkId(linkId)
                .surveyId(request.getSurveyId())
                .targetUrl(request.getTargetUrl())
                .expiresAt(request.getExpiresAt())
                .active(true)
                .clicks(0L)
                .build();

        SurveyLink savedEntity = surveyLinkRepository.save(surveyLink);

        return linkMapper.toResponse(savedEntity);
    }

    @Transactional(readOnly = true)
    public LinkResponse getLinkByLinkId(String linkId) {
        return surveyLinkRepository.findByLinkId(linkId)
                .map(linkMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Link not found with ID: " + linkId));
    }
}
