package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.VenueRequest;
import com.jugger.afc.dto.VenueResponse;
import com.jugger.afc.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ADMIN_VENUES)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VenueAdminController {
    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@RequestBody VenueRequest request) {
        VenueResponse response = venueService.createVenue(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<VenueResponse> getVenue(@PathVariable UUID venueId) {
        return ResponseEntity.ok(venueService.getVenue(venueId));
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable UUID venueId, @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.updateVenue(venueId, request));
    }

    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> deleteVenue(@PathVariable UUID venueId) {
        venueService.deleteVenue(venueId);
        return ResponseEntity.noContent().build();
    }
}
