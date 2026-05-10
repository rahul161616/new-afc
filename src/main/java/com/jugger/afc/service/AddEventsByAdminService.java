package com.jugger.afc.service;

import com.jugger.afc.dto.AddEventsAdminRequest;
import com.jugger.afc.dto.AddEventsAdminResponse;
import com.jugger.afc.entity.AppUser;
import com.jugger.afc.entity.FutsalEvent;
import com.jugger.afc.enums.EventStatus;
import com.jugger.afc.repository.FutsalEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddEventsByAdminService {
    private final FutsalEventRepository futsalEventRepository;
    private final CurrentUserService currentUserService;
    private final EventPermissionService eventPermissionService;

    public AddEventsAdminResponse addEventsByAdmin(AddEventsAdminRequest addEventsAdminRequest){
        if (addEventsAdminRequest == null) return null;
        if(addEventsAdminRequest.getGroupId() == null) {
            throw new IllegalArgumentException("Group id cannot be null");
        }
        if(addEventsAdminRequest.getVenueId() == null) {
            throw new IllegalArgumentException("Venue id cannot be null");
        }
        if (addEventsAdminRequest.getRequiredPlayers() == null || addEventsAdminRequest.getMaxPlayers() == null) {
            throw new IllegalArgumentException("Player counts cannot be null");
        }
        if (addEventsAdminRequest.getRequiredPlayers() <= 0) {
            throw new IllegalArgumentException("Required players must be greater than 0");
        }
        if (addEventsAdminRequest.getMaxPlayers() < addEventsAdminRequest.getRequiredPlayers()) {
            throw new IllegalArgumentException("Max players must be greater than or equal to required players");
        }
        Instant now = Instant.now();
        AppUser currentUser = currentUserService.requireCurrentUser();
        eventPermissionService.ensureCanCreateEvent(addEventsAdminRequest.getGroupId(), currentUser);
        FutsalEvent event = FutsalEvent.builder()
                .groupId(addEventsAdminRequest.getGroupId())
                .venueId(addEventsAdminRequest.getVenueId())
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
                .build();
        FutsalEvent savedEvent = futsalEventRepository.save(event);
        return AddEventsAdminResponse.builder()
                .id(savedEvent.getId())
                .groupId(savedEvent.getGroupId())
                .venueId(savedEvent.getVenueId())
                .title(savedEvent.getTitle())
                .description(savedEvent.getDescription())
                .startTime(savedEvent.getStartTime())
                .endTime(savedEvent.getEndTime())
                .requiredPlayers(savedEvent.getRequiredPlayers())
                .maxPlayers(savedEvent.getMaxPlayers())
                .status(savedEvent.getStatus())
                .createdBy(savedEvent.getCreatedBy())
                .createdAt(savedEvent.getCreatedAt())
                .updatedAt(savedEvent.getUpdatedAt())
                .build();
    }

    public AddEventsAdminResponse updateEvent(UUID eventId, AddEventsAdminRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event update request cannot be null");
        }
        validateEventPayload(request);

        FutsalEvent event = futsalEventRepository.findByIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        AppUser currentUser = currentUserService.requireCurrentUser();
        eventPermissionService.ensureCanManageEvent(event, currentUser);

        if (request.getGroupId() != null && !event.getGroupId().equals(request.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event group cannot be changed");
        }

        event.setVenueId(request.getVenueId());
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setRequiredPlayers(request.getRequiredPlayers());
        event.setMaxPlayers(request.getMaxPlayers());
        event.setUpdatedAt(Instant.now());

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

    private void validateEventPayload(AddEventsAdminRequest request) {
        if (request.getVenueId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue id cannot be null");
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
        return AddEventsAdminResponse.builder()
                .id(event.getId())
                .groupId(event.getGroupId())
                .venueId(event.getVenueId())
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
}
