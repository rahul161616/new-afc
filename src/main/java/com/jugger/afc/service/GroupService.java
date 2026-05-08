package com.jugger.afc.service;

import com.jugger.afc.dto.GroupMemberRequest;
import com.jugger.afc.dto.GroupMemberResponse;
import com.jugger.afc.dto.GroupRequest;
import com.jugger.afc.dto.GroupResponse;
import com.jugger.afc.entity.AppUser;
import com.jugger.afc.entity.Group;
import com.jugger.afc.entity.GroupMember;
import com.jugger.afc.enums.GroupMemberRole;
import com.jugger.afc.enums.GroupMemberStatus;
import com.jugger.afc.enums.UserRole;
import com.jugger.afc.repository.GroupMemberRepository;
import com.jugger.afc.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CurrentUserService currentUserService;

    public GroupResponse createGroup(GroupRequest request) {
        validateGroupRequest(request);

        AppUser currentUser = currentUserService.requireCurrentUser();
        Instant now = Instant.now();
        Group group = Group.builder()
                .name(request.getName().trim())
                .createdBy(currentUser.getId())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Group savedGroup = groupRepository.save(group);
        GroupMember ownerMembership = GroupMember.builder()
                .groupId(savedGroup.getId())
                .userId(currentUser.getId())
                .role(GroupMemberRole.OWNER)
                .status(GroupMemberStatus.APPROVED)
                .joinedAt(now)
                .isActive(Boolean.TRUE)
                .build();
        groupMemberRepository.save(ownerMembership);

        return toGroupResponse(savedGroup);
    }

    public List<GroupResponse> getGroups() {
        return groupRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::toGroupResponse)
                .toList();
    }

    public List<GroupResponse> getMyGroups() {
        AppUser currentUser = currentUserService.requireCurrentUser();

        return groupMemberRepository.findAllByUserId(currentUser.getId())
                .stream()
                .map(member -> getActiveGroup(member.getGroupId()))
                .map(this::toGroupResponse)
                .toList();
    }

    public GroupResponse getGroup(UUID groupId) {
        return toGroupResponse(getActiveGroup(groupId));
    }

    public GroupMemberResponse joinGroup(UUID groupId) {
        AppUser currentUser = currentUserService.requireCurrentUser();
        Group group = getActiveGroup(groupId);

        GroupMember member = requestMembership(group.getId(), currentUser.getId());
        return toMemberResponse(member, currentUser);
    }

    public GroupMemberResponse addMember(UUID groupId, GroupMemberRequest request) {
        getActiveGroup(groupId);
        if (request == null || request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }

        AppUser user = currentUserService.requireUserById(request.getUserId());
        GroupMemberRole role = request.getRole() == null ? GroupMemberRole.PLAYER : request.getRole();
        GroupMember member = upsertMembership(groupId, user.getId(), role, GroupMemberStatus.APPROVED, true);
        return toMemberResponse(member, user);
    }

    public List<GroupMemberResponse> getMembers(UUID groupId) {
        getActiveGroup(groupId);

        return groupMemberRepository.findAllByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(groupId)
                .stream()
                .map(member -> toMemberResponse(member, currentUserService.requireUserById(member.getUserId())))
                .toList();
    }

    public List<GroupMemberResponse> getPendingMembers(UUID groupId) {
        Group group = getActiveGroup(groupId);
        ensureCanReviewGroup(group);

        return groupMemberRepository.findAllByGroupIdAndStatusOrderByJoinedAtAsc(groupId, GroupMemberStatus.PENDING)
                .stream()
                .map(member -> toMemberResponse(member, currentUserService.requireUserById(member.getUserId())))
                .toList();
    }

    public GroupMemberResponse approveMember(UUID groupId, UUID memberId) {
        Group group = getActiveGroup(groupId);
        ensureCanReviewGroup(group);
        GroupMember member = getGroupMember(groupId, memberId);
        member.setStatus(GroupMemberStatus.APPROVED);
        member.setIsActive(Boolean.TRUE);

        GroupMember savedMember = groupMemberRepository.save(member);
        return toMemberResponse(savedMember, currentUserService.requireUserById(savedMember.getUserId()));
    }

    public GroupMemberResponse rejectMember(UUID groupId, UUID memberId) {
        Group group = getActiveGroup(groupId);
        ensureCanReviewGroup(group);
        GroupMember member = getGroupMember(groupId, memberId);
        member.setStatus(GroupMemberStatus.REJECTED);
        member.setIsActive(Boolean.FALSE);

        GroupMember savedMember = groupMemberRepository.save(member);
        return toMemberResponse(savedMember, currentUserService.requireUserById(savedMember.getUserId()));
    }

    public void deleteGroup(UUID groupId) {
        Group group = getActiveGroup(groupId);
        group.setDeletedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        groupRepository.save(group);
    }

    private GroupMember requestMembership(UUID groupId, UUID userId) {
        GroupMember existingMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId).orElse(null);
        if (existingMember != null && existingMember.getStatus() == GroupMemberStatus.APPROVED && Boolean.TRUE.equals(existingMember.getIsActive())) {
            return existingMember;
        }

        return upsertMembership(groupId, userId, GroupMemberRole.PLAYER, GroupMemberStatus.PENDING, false);
    }

    private GroupMember upsertMembership(UUID groupId, UUID userId, GroupMemberRole role, GroupMemberStatus status, boolean isActive) {
        Instant now = Instant.now();
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseGet(() -> GroupMember.builder()
                        .groupId(groupId)
                        .userId(userId)
                        .joinedAt(now)
                        .build());

        member.setRole(role);
        member.setStatus(status);
        member.setIsActive(isActive);
        if (member.getJoinedAt() == null) {
            member.setJoinedAt(now);
        }

        return groupMemberRepository.save(member);
    }

    private GroupMember getGroupMember(UUID groupId, UUID memberId) {
        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group member not found"));
        if (!groupId.equals(member.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group member not found");
        }
        return member;
    }

    private void ensureCanReviewGroup(Group group) {
        AppUser currentUser = currentUserService.requireCurrentUser();
        boolean isCreator = currentUser.getId().equals(group.getCreatedBy());
        boolean hasGlobalReviewRole = currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.LEADER;

        if (!isCreator && !hasGlobalReviewRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group creator, leader, or admin can review join requests");
        }
    }

    private Group getActiveGroup(UUID groupId) {
        return groupRepository.findByIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    private void validateGroupRequest(GroupRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group request cannot be null");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name cannot be blank");
        }
    }

    private GroupResponse toGroupResponse(Group group) {
        int activeMemberCount = (int) groupMemberRepository.countByGroupIdAndStatus(group.getId(), GroupMemberStatus.APPROVED);
        AppUser currentUser = currentUserService.findCurrentUser().orElse(null);
        GroupMember currentUserMembership = currentUser == null
                ? null
                : groupMemberRepository.findByGroupIdAndUserId(group.getId(), currentUser.getId()).orElse(null);
        boolean currentUserCreatedGroup = currentUser != null && currentUser.getId().equals(group.getCreatedBy());

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .activeMemberCount(activeMemberCount)
                .currentUserRole(resolveCurrentUserRole(currentUserMembership, currentUserCreatedGroup))
                .currentUserStatus(resolveCurrentUserStatus(currentUserMembership, currentUserCreatedGroup))
                .build();
    }

    private GroupMemberRole resolveCurrentUserRole(GroupMember membership, boolean currentUserCreatedGroup) {
        if (membership != null && membership.getRole() != null) {
            return membership.getRole();
        }
        return currentUserCreatedGroup ? GroupMemberRole.OWNER : null;
    }

    private GroupMemberStatus resolveCurrentUserStatus(GroupMember membership, boolean currentUserCreatedGroup) {
        if (membership != null && membership.getStatus() != null) {
            return membership.getStatus();
        }
        return currentUserCreatedGroup ? GroupMemberStatus.APPROVED : null;
    }

    private GroupMemberResponse toMemberResponse(GroupMember member, AppUser user) {
        return GroupMemberResponse.builder()
                .id(member.getId())
                .groupId(member.getGroupId())
                .userId(member.getUserId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .role(member.getRole())
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .isActive(member.getIsActive())
                .build();
    }
}
