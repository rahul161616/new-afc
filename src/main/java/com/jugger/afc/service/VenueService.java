package com.jugger.afc.service;

import com.jugger.afc.dto.VenueRequest;
import com.jugger.afc.dto.VenueResponse;
import com.jugger.afc.entity.Venue;
import com.jugger.afc.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;

    public VenueResponse createVenue(VenueRequest request) {
        validateRequest(request);

        Instant now = Instant.now();
        Venue venue = Venue.builder()
                .id(UUID.randomUUID())
                .groupId(request.getGroupId())
                .name(request.getName().trim())
                .address(request.getAddress())
                .mapUrl(request.getMapUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return toResponse(venueRepository.save(venue));
    }

    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VenueResponse getVenue(UUID venueId) {
        return toResponse(getActiveVenue(venueId));
    }

    public VenueResponse updateVenue(UUID venueId, VenueRequest request) {
        validateRequest(request);

        Venue venue = getActiveVenue(venueId);
        venue.setGroupId(request.getGroupId());
        venue.setName(request.getName().trim());
        venue.setAddress(request.getAddress());
        venue.setMapUrl(request.getMapUrl());
        venue.setLatitude(request.getLatitude());
        venue.setLongitude(request.getLongitude());
        venue.setIsActive(request.getIsActive() != null ? request.getIsActive() : venue.getIsActive());
        venue.setUpdatedAt(Instant.now());

        return toResponse(venueRepository.save(venue));
    }

    public void deleteVenue(UUID venueId) {
        Venue venue = getActiveVenue(venueId);
        venue.setIsActive(Boolean.FALSE);
        venue.setDeletedAt(Instant.now());
        venue.setUpdatedAt(Instant.now());
        venueRepository.save(venue);
    }

    private Venue getActiveVenue(UUID venueId) {
        return venueRepository.findByIdAndDeletedAtIsNull(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));
    }

    private void validateRequest(VenueRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue request cannot be null");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue name cannot be blank");
        }
    }

    private VenueResponse toResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .groupId(venue.getGroupId())
                .name(venue.getName())
                .address(venue.getAddress())
                .mapUrl(venue.getMapUrl())
                .latitude(venue.getLatitude())
                .longitude(venue.getLongitude())
                .isActive(venue.getIsActive())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .build();
    }
}
