package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.EventInterestResponse;
import com.jugger.afc.service.EventInterestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ADMIN_EVENTS)
@PreAuthorize("hasAnyRole('ADMIN','LEADER')")
public class AdminEventController {
    private final EventInterestService eventInterestService;

    public AdminEventController(EventInterestService eventInterestService) {
        this.eventInterestService = eventInterestService;
    }

    @GetMapping({"/{eventId}/interests", "/{eventId}/responses"})
    public ResponseEntity<List<EventInterestResponse>> getEventInterests(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventInterestService.getEventInterests(eventId));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventInterestService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
