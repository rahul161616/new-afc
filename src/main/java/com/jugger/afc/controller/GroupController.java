package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.GroupMemberRequest;
import com.jugger.afc.dto.GroupMemberResponse;
import com.jugger.afc.dto.GroupRequest;
import com.jugger.afc.dto.GroupResponse;
import com.jugger.afc.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.GROUPS)
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups() {
        return ResponseEntity.ok(groupService.getGroups());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(groupService.getMyGroups());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @PostMapping("/{groupId}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GroupMemberResponse> joinGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.joinGroup(groupId));
    }

    @PostMapping("/{groupId}/members")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public ResponseEntity<GroupMemberResponse> addMember(
            @PathVariable UUID groupId,
            @RequestBody GroupMemberRequest request
    ) {
        GroupMemberResponse response = groupService.addMember(groupId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getMembers(groupId));
    }

    @GetMapping("/{groupId}/join-requests")
    public ResponseEntity<List<GroupMemberResponse>> getPendingMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getPendingMembers(groupId));
    }

    @PostMapping("/{groupId}/members/{memberId}/approve")
    public ResponseEntity<GroupMemberResponse> approveMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId
    ) {
        return ResponseEntity.ok(groupService.approveMember(groupId, memberId));
    }

    @PostMapping("/{groupId}/members/{memberId}/reject")
    public ResponseEntity<GroupMemberResponse> rejectMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId
    ) {
        return ResponseEntity.ok(groupService.rejectMember(groupId, memberId));
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}
