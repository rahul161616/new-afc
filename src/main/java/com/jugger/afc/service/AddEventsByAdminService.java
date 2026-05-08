package com.jugger.afc.service;

import com.jugger.afc.dto.AddEventsAdminRequest;
import com.jugger.afc.dto.AddEventsAdminResponse;
import com.jugger.afc.entity.AppUser;
import com.jugger.afc.entity.FutsalEvent;
import com.jugger.afc.entity.Group;
import com.jugger.afc.entity.GroupMember;
import com.jugger.afc.enums.EventStatus;
import com.jugger.afc.enums.GroupMemberRole;
import com.jugger.afc.enums.GroupMemberStatus;
import com.jugger.afc.enums.UserRole;
import com.jugger.afc.repository.FutsalEventRepository;
import com.jugger.afc.repository.GroupMemberRepository;
import com.jugger.afc.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddEventsByAdminService {
    private final FutsalEventRepository futsalEventRepository;
    private final CurrentUserService currentUserService;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

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
        ensureCanCreateEvent(addEventsAdminRequest, currentUser);
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

    private void ensureCanCreateEvent(AddEventsAdminRequest request, AppUser currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.LEADER) {
            return;
        }

        Group group = groupRepository.findByIdAndDeletedAtIsNull(request.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (currentUser.getId().equals(group.getCreatedBy())) {
            return;
        }

        Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(
                request.getGroupId(),
                currentUser.getId()
        );

        boolean canCreateForGroup = membership
                .filter(member -> member.getStatus() == GroupMemberStatus.APPROVED)
                .filter(member -> member.getRole() == GroupMemberRole.OWNER || member.getRole() == GroupMemberRole.ORGANIZER)
                .isPresent();

        if (!canCreateForGroup) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group leaders can create events for this group");
        }
    }
}
