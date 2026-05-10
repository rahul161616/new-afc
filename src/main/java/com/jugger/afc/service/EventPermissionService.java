package com.jugger.afc.service;

import com.jugger.afc.entity.AppUser;
import com.jugger.afc.entity.FutsalEvent;
import com.jugger.afc.entity.Group;
import com.jugger.afc.entity.GroupMember;
import com.jugger.afc.enums.GroupMemberRole;
import com.jugger.afc.enums.GroupMemberStatus;
import com.jugger.afc.enums.UserRole;
import com.jugger.afc.repository.GroupMemberRepository;
import com.jugger.afc.repository.GroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class EventPermissionService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public EventPermissionService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public void ensureCanCreateEvent(UUID groupId, AppUser currentUser) {
        if (canManageGroup(groupId, currentUser)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group leaders can create events for this group");
    }

    public void ensureCanManageEvent(FutsalEvent event, AppUser currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.LEADER) {
            return;
        }

        if (currentUser.getId().equals(event.getCreatedBy())) {
            return;
        }

        if (canManageGroup(event.getGroupId(), currentUser)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the event creator or group leader can manage this event");
    }

    private boolean canManageGroup(UUID groupId, AppUser currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.LEADER) {
            return true;
        }

        Group group = groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (currentUser.getId().equals(group.getCreatedBy())) {
            return true;
        }

        Optional<GroupMember> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId());
        return membership
                .filter(member -> member.getStatus() == GroupMemberStatus.APPROVED)
                .filter(member -> member.getRole() == GroupMemberRole.OWNER || member.getRole() == GroupMemberRole.ORGANIZER)
                .isPresent();
    }
}
