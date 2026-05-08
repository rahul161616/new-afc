package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.EventDetailResponse;
import com.jugger.afc.dto.EventInterestRequest;
import com.jugger.afc.dto.EventInterestResponse;
import com.jugger.afc.dto.EventSummaryResponse;
import com.jugger.afc.service.EventInterestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.EVENTS)
public class EventController {
    private final EventInterestService eventInterestService;

    public EventController(EventInterestService eventInterestService) {
        this.eventInterestService = eventInterestService;
    }

    @GetMapping
    public ResponseEntity<List<EventSummaryResponse>> getEvents() {
        return ResponseEntity.ok(eventInterestService.getVisibleEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponse> getEventDetail(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventInterestService.getEventDetail(eventId));
    }

    @PostMapping({"/{eventId}/interests", "/{eventId}/responses"})
    public ResponseEntity<EventInterestResponse> expressInterest(
            @PathVariable UUID eventId,
            @RequestBody(required = false) EventInterestRequest request
    ) {
        return ResponseEntity.ok(eventInterestService.expressInterest(eventId, request));
    }
}
