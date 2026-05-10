package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.AddEventsAdminRequest;
import com.jugger.afc.dto.AddEventsAdminResponse;
import com.jugger.afc.dto.EventInterestResponse;
import com.jugger.afc.service.AddEventsByAdminService;
import com.jugger.afc.service.EventInterestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ADMIN_EVENTS)
public class AdminEventController {
    private final EventInterestService eventInterestService;
    private final AddEventsByAdminService addEventsByAdminService;

    public AdminEventController(EventInterestService eventInterestService, AddEventsByAdminService addEventsByAdminService) {
        this.eventInterestService = eventInterestService;
        this.addEventsByAdminService = addEventsByAdminService;
    }

    @GetMapping({"/{eventId}/interests", "/{eventId}/responses"})
    public ResponseEntity<List<EventInterestResponse>> getEventInterests(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventInterestService.getEventInterests(eventId));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<AddEventsAdminResponse> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody AddEventsAdminRequest request
    ) {
        return ResponseEntity.ok(addEventsByAdminService.updateEvent(eventId, request));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        addEventsByAdminService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
