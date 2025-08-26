package com.survery.links;

import com.survery.links.dto.CreateLinkRequest;
import com.survery.links.dto.LinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(@RequestBody CreateLinkRequest request) {
        LinkResponse response = linkService.createLink(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{linkId}")
    public ResponseEntity<LinkResponse> getLink(@PathVariable String linkId) {
        LinkResponse response = linkService.getLinkByLinkId(linkId);
        return ResponseEntity.ok(response);
    }
}
