package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.LeaderApplicationResponse;
import com.jugger.afc.service.LeaderApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class LeaderApplicationController {
    private final LeaderApplicationService leaderApplicationService;

    public LeaderApplicationController(LeaderApplicationService leaderApplicationService) {
        this.leaderApplicationService = leaderApplicationService;
    }

    @PostMapping(ApiConstants.USERS + "/leader-application")
    public ResponseEntity<LeaderApplicationResponse> apply() {
        return ResponseEntity.ok(leaderApplicationService.apply());
    }

    @GetMapping(ApiConstants.USERS + "/leader-application")
    public ResponseEntity<LeaderApplicationResponse> myStatus() {
        return ResponseEntity.ok(leaderApplicationService.getMyStatus());
    }

    @GetMapping(ApiConstants.ADMIN + "/leader-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaderApplicationResponse>> getPendingApplications() {
        return ResponseEntity.ok(leaderApplicationService.getPendingApplications());
    }

    @PostMapping(ApiConstants.ADMIN + "/leader-applications/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaderApplicationResponse> approve(@PathVariable UUID userId) {
        return ResponseEntity.ok(leaderApplicationService.approve(userId));
    }

    @PostMapping(ApiConstants.ADMIN + "/leader-applications/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaderApplicationResponse> reject(@PathVariable UUID userId) {
        return ResponseEntity.ok(leaderApplicationService.reject(userId));
    }
}
