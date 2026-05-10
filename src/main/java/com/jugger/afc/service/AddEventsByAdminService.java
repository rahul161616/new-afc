package com.jugger.afc.service;

import com.jugger.afc.dto.AddEventsAdminRequest;
import com.jugger.afc.dto.AddEventsAdminResponse;
import com.jugger.afc.entity.AppUser;
import com.jugger.afc.entity.FutsalEvent;
import com.jugger.afc.entity.Venue;
import com.jugger.afc.enums.EventStatus;
import com.jugger.afc.repository.FutsalEventRepository;
import com.jugger.afc.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddEventsByAdminService {
    private final FutsalEventRepository futsalEventRepository;
    private final VenueRepository venueRepository;
    private final CurrentUserService currentUserService;
    private final EventPermissionService eventPermissionService;

    public AddEventsAdminResponse addEventsByAdmin(AddEventsAdminRequest addEventsAdminRequest){
        if (addEventsAdminRequest == null) return null;
        if(addEventsAdminRequest.getGroupId() == null) {
            throw new IllegalArgumentException("Group id cannot be null");
        }
        List<UUID> venueIds = resolveVenueIds(addEventsAdminRequest);
        validateEventPayload(addEventsAdminRequest, venueIds);
        List<Venue> venues = loadVenues(venueIds);
        Instant now = Instant.now();
        AppUser currentUser = currentUserService.requireCurrentUser();
        eventPermissionService.ensureCanCreateEvent(addEventsAdminRequest.getGroupId(), currentUser);
        FutsalEvent event = FutsalEvent.builder()
                .groupId(addEventsAdminRequest.getGroupId())
                .venueId(venues.get(0).getId())
                .title(addEventsAdminRequest.getTitle())
                .description(addEventsAdminRequest.getDescription())
                .startTime(addEventsAdminRequest.getStartTime())
                .endTime(addEventsAdminRequest.getEndTime())
                .requiredPlayers(addEventsAdminRequest.getRequiredPlayers())
                .maxPlayers(addEventsAdminRequest.getMaxPlayers())
                .status(EventStatus.PLANNING)
                .createdBy(currentUser.getId())
                .createdAt(now)
                .updatedAt(now)
                .venues(new LinkedHashSet<>(venues))
                .build();
        FutsalEvent savedEvent = futsalEventRepository.save(event);
        return toResponse(savedEvent);
    }

    public AddEventsAdminResponse updateEvent(UUID eventId, AddEventsAdminRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event update request cannot be null");
        }
        List<UUID> venueIds = resolveVenueIds(request);
        validateEventPayload(request, venueIds);
        List<Venue> venues = loadVenues(venueIds);

        FutsalEvent event = futsalEventRepository.findByIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        AppUser currentUser = currentUserService.requireCurrentUser();
        eventPermissionService.ensureCanManageEvent(event, currentUser);

        if (request.getGroupId() != null && !event.getGroupId().equals(request.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event group cannot be changed");
        }

        event.setVenueId(venues.get(0).getId());
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setRequiredPlayers(request.getRequiredPlayers());
        event.setMaxPlayers(request.getMaxPlayers());
        event.setUpdatedAt(Instant.now());
        event.setVenues(new LinkedHashSet<>(venues));

        return toResponse(futsalEventRepository.save(event));
    }

    public void deleteEvent(UUID eventId) {
        FutsalEvent event = futsalEventRepository.findByIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        AppUser currentUser = currentUserService.requireCurrentUser();
        eventPermissionService.ensureCanManageEvent(event, currentUser);

        event.setDeletedAt(Instant.now());
        event.setUpdatedAt(Instant.now());
        futsalEventRepository.save(event);
    }

    private void validateEventPayload(AddEventsAdminRequest request, List<UUID> venueIds) {
        if (venueIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one venue is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be blank");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end time are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        if (request.getRequiredPlayers() == null || request.getMaxPlayers() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player counts cannot be null");
        }
        if (request.getRequiredPlayers() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required players must be greater than 0");
        }
        if (request.getMaxPlayers() < request.getRequiredPlayers()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max players must be greater than or equal to required players");
        }
    }

    private AddEventsAdminResponse toResponse(FutsalEvent event) {
        List<Venue> venues = sortedVenues(event);
        return AddEventsAdminResponse.builder()
                .id(event.getId())
                .groupId(event.getGroupId())
                .venueId(event.getVenueId())
                .venueIds(venues.stream().map(Venue::getId).toList())
                .venueNames(venues.stream().map(Venue::getName).toList())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .requiredPlayers(event.getRequiredPlayers())
                .maxPlayers(event.getMaxPlayers())
                .status(event.getStatus())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private List<UUID> resolveVenueIds(AddEventsAdminRequest request) {
        List<UUID> venueIds = request.getVenueIds();
        if (venueIds != null && !venueIds.isEmpty()) {
            return new ArrayList<>(venueIds.stream().filter(java.util.Objects::nonNull).distinct().toList());
        }

        if (request.getVenueId() != null) {
            return List.of(request.getVenueId());
        }

        return List.of();
    }

    private List<Venue> loadVenues(List<UUID> venueIds) {
        return venueIds.stream()
                .map(this::getActiveVenue)
                .collect(Collectors.toList());
    }

    private Venue getActiveVenue(UUID venueId) {
        return venueRepository.findByIdAndDeletedAtIsNull(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));
    }

    private List<Venue> sortedVenues(FutsalEvent event) {
        return event.getVenues().stream()
                .sorted((left, right) -> {
                    String leftName = left.getName() == null ? "" : left.getName();
                    String rightName = right.getName() == null ? "" : right.getName();
                    int nameCompare = leftName.compareToIgnoreCase(rightName);
                    return nameCompare != 0 ? nameCompare : left.getId().compareTo(right.getId());
                })
                .toList();
    }
}
