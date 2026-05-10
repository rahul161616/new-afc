package com.jugger.afc.service;

import com.jugger.afc.dto.EventDetailResponse;
import com.jugger.afc.dto.EventInterestRequest;
import com.jugger.afc.dto.EventInterestResponse;
import com.jugger.afc.dto.EventSummaryResponse;
import com.jugger.afc.entity.AppUser;
import com.jugger.afc.entity.EventResponse;
import com.jugger.afc.entity.FutsalEvent;
import com.jugger.afc.enums.EventInterestStatus;
import com.jugger.afc.enums.EventStatus;
import com.jugger.afc.repository.EventResponseRepository;
import com.jugger.afc.repository.FutsalEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EventInterestService {
    private final FutsalEventRepository futsalEventRepository;
    private final EventResponseRepository eventResponseRepository;
    private final CurrentUserService currentUserService;
    private final EventPermissionService eventPermissionService;

    public EventInterestService(
            FutsalEventRepository futsalEventRepository,
            EventResponseRepository eventResponseRepository,
            CurrentUserService currentUserService,
            EventPermissionService eventPermissionService
    ) {
        this.futsalEventRepository = futsalEventRepository;
        this.eventResponseRepository = eventResponseRepository;
        this.currentUserService = currentUserService;
        this.eventPermissionService = eventPermissionService;
    }

    public List<EventSummaryResponse> getVisibleEvents() {
        List<FutsalEvent> events = futsalEventRepository.findAllByDeletedAtIsNullOrderByStartTimeAsc();
        Map<UUID, List<EventResponse>> responsesByEventId = loadResponsesByEventId(events);
        Optional<AppUser> currentUser = currentUserService.findCurrentUser();

        return events.stream()
                .map(event -> toEventSummary(event, responsesByEventId.getOrDefault(event.getId(), List.of()), currentUser))
                .toList();
    }

    public EventDetailResponse getEventDetail(UUID eventId) {
        FutsalEvent event = getActiveEvent(eventId);
        List<EventResponse> responses = eventResponseRepository.findAllByEventIdOrderByRespondedAtAsc(eventId);
        Optional<AppUser> currentUser = currentUserService.findCurrentUser();
        Map<UUID, AppUser> usersById = getUsersById(responses);
        ResponseCounts counts = buildCounts(responses);

        return EventDetailResponse.builder()
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
                .currentUserResponseStatus(findCurrentUserResponseStatus(responses, currentUser))
                .interestedCount(counts.interestedCount())
                .confirmedCount(counts.confirmedCount())
                .goingCount(counts.goingCount())
                .maybeCount(counts.maybeCount())
                .notAvailableCount(counts.notAvailableCount())
                .waitlistedCount(counts.waitlistedCount())
                .droppedCount(counts.droppedCount())
                .participants(responses.stream()
                        .map(response -> toInterestResponse(response, usersById.get(response.getUserId())))
                        .toList())
                .build();
    }

    public EventInterestResponse expressInterest(UUID eventId, EventInterestRequest request) {
        AppUser currentUser = currentUserService.requireCurrentUser();
        FutsalEvent event = getActiveEvent(eventId);
        Instant now = Instant.now();

        EventResponse eventResponse = eventResponseRepository.findByEventIdAndUserId(eventId, currentUser.getId())
                .orElseGet(() -> EventResponse.builder()
                        .id(UUID.randomUUID())
                        .eventId(event.getId())
                        .userId(currentUser.getId())
                        .build());

        EventInterestStatus requestedStatus = request != null && request.getStatus() != null
                ? request.getStatus()
                : EventInterestStatus.INTERESTED;
        EventInterestStatus normalizedRequestedStatus = normalizeRequestedStatus(requestedStatus);

        if (normalizedRequestedStatus == EventInterestStatus.WAITLISTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WAITLISTED is assigned by the system");
        }

        if (normalizedRequestedStatus == EventInterestStatus.DROPPED
                && event.getStatus() == EventStatus.CONFIRMED
                && (request == null || request.getDropReason() == null || request.getDropReason().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Drop reason is required after confirmation");
        }

        EventInterestStatus finalStatus = determineFinalStatus(event, eventResponse, normalizedRequestedStatus, now);

        eventResponse.setResponseStatus(finalStatus);
        eventResponse.setNote(request == null ? null : request.getNote());
        eventResponse.setDropReason(finalStatus == EventInterestStatus.DROPPED && request != null ? request.getDropReason() : null);
        eventResponse.setRespondedAt(now);
        eventResponse.setUpdatedAt(now);

        if (finalStatus != EventInterestStatus.WAITLISTED) {
            eventResponse.setWaitlistPosition(null);
            eventResponse.setJoinedWaitlistAt(null);
        }

        return toInterestResponse(eventResponseRepository.save(eventResponse), currentUser);
    }

    public List<EventInterestResponse> getEventInterests(UUID eventId) {
        FutsalEvent event = getActiveEvent(eventId);
        AppUser currentUser = currentUserService.requireCurrentUser();
        eventPermissionService.ensureCanManageEvent(event, currentUser);

        return eventResponseRepository.findAllByEventIdOrderByRespondedAtAsc(eventId)
                .stream()
                .map(response -> {
                    AppUser user = currentUserService.requireUserById(response.getUserId());
                    return toInterestResponse(response, user);
                })
                .toList();
    }

    private FutsalEvent getActiveEvent(UUID eventId) {
        return futsalEventRepository.findByIdAndDeletedAtIsNull(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    private EventSummaryResponse toEventSummary(
            FutsalEvent event,
            List<EventResponse> responses,
            Optional<AppUser> currentUser
    ) {
        ResponseCounts counts = buildCounts(responses);

        return EventSummaryResponse.builder()
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
                .currentUserResponseStatus(findCurrentUserResponseStatus(responses, currentUser))
                .interestedCount(counts.interestedCount())
                .confirmedCount(counts.confirmedCount())
                .goingCount(counts.goingCount())
                .maybeCount(counts.maybeCount())
                .notAvailableCount(counts.notAvailableCount())
                .waitlistedCount(counts.waitlistedCount())
                .droppedCount(counts.droppedCount())
                .build();
    }

    private EventInterestResponse toInterestResponse(EventResponse response, AppUser user) {
        return EventInterestResponse.builder()
                .id(response.getId())
                .eventId(response.getEventId())
                .userId(response.getUserId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .status(response.getResponseStatus().toApiStatus())
                .note(response.getNote())
                .dropReason(response.getDropReason())
                .waitlistPosition(response.getWaitlistPosition())
                .respondedAt(response.getRespondedAt())
                .build();
    }

    private Map<UUID, List<EventResponse>> loadResponsesByEventId(List<FutsalEvent> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        return eventResponseRepository.findAllByEventIdIn(events.stream().map(FutsalEvent::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(EventResponse::getEventId));
    }

    private Map<UUID, AppUser> getUsersById(List<EventResponse> responses) {
        return responses.stream()
                .map(EventResponse::getUserId)
                .distinct()
                .map(currentUserService::requireUserById)
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));
    }

    private EventInterestStatus findCurrentUserResponseStatus(List<EventResponse> responses, Optional<AppUser> currentUser) {
        if (currentUser.isEmpty()) {
            return null;
        }

        UUID currentUserId = currentUser.get().getId();
        return responses.stream()
                .filter(response -> currentUserId.equals(response.getUserId()))
                .map(EventResponse::getResponseStatus)
                .map(EventInterestStatus::toApiStatus)
                .findFirst()
                .orElse(null);
    }

    private ResponseCounts buildCounts(List<EventResponse> responses) {
        EnumMap<EventInterestStatus, Integer> counts = new EnumMap<>(EventInterestStatus.class);

        for (EventInterestStatus status : EventInterestStatus.values()) {
            counts.put(status, 0);
        }

        for (EventResponse response : responses) {
            counts.computeIfPresent(response.getResponseStatus(), (status, count) -> count + 1);
        }

        return new ResponseCounts(
                counts.get(EventInterestStatus.INTERESTED),
                counts.get(EventInterestStatus.CONFIRMED) + counts.get(EventInterestStatus.GOING),
                counts.get(EventInterestStatus.GOING),
                counts.get(EventInterestStatus.MAYBE),
                counts.get(EventInterestStatus.NOT_AVAILABLE),
                counts.get(EventInterestStatus.WAITLISTED),
                counts.get(EventInterestStatus.DROPPED)
        );
    }

    private EventInterestStatus normalizeRequestedStatus(EventInterestStatus requestedStatus) {
        return requestedStatus == EventInterestStatus.GOING ? EventInterestStatus.CONFIRMED : requestedStatus;
    }

    private EventInterestStatus determineFinalStatus(
            FutsalEvent event,
            EventResponse existingResponse,
            EventInterestStatus requestedStatus,
            Instant now
    ) {
        if (requestedStatus != EventInterestStatus.CONFIRMED) {
            return requestedStatus;
        }

        long confirmedCount = eventResponseRepository.countByEventIdAndResponseStatusIn(
                event.getId(),
                List.of(EventInterestStatus.CONFIRMED, EventInterestStatus.GOING)
        );

        if (existingResponse.getResponseStatus() != null && existingResponse.getResponseStatus().countsAsConfirmed()) {
            confirmedCount--;
        }

        if (confirmedCount >= event.getMaxPlayers()) {
            if (existingResponse.getJoinedWaitlistAt() == null) {
                existingResponse.setJoinedWaitlistAt(now);
            }
            if (existingResponse.getWaitlistPosition() == null) {
                existingResponse.setWaitlistPosition(calculateNextWaitlistPosition(event.getId(), existingResponse));
            }
            return EventInterestStatus.WAITLISTED;
        }

        return EventInterestStatus.CONFIRMED;
    }

    private int calculateNextWaitlistPosition(UUID eventId, EventResponse existingResponse) {
        return eventResponseRepository.findAllByEventIdOrderByRespondedAtAsc(eventId)
                .stream()
                .filter(response -> !response.getId().equals(existingResponse.getId()))
                .map(EventResponse::getWaitlistPosition)
                .filter(position -> position != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private record ResponseCounts(
            int interestedCount,
            int confirmedCount,
            int goingCount,
            int maybeCount,
            int notAvailableCount,
            int waitlistedCount,
            int droppedCount
    ) {
    }
}
