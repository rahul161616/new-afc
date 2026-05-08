package com.jugger.afc.service;

import com.jugger.afc.dto.LeaderApplicationResponse;
import com.jugger.afc.entity.AppUser;
import com.jugger.afc.enums.LeaderApplicationStatus;
import com.jugger.afc.enums.UserRole;
import com.jugger.afc.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class LeaderApplicationService {
    private final AppUserRepository appUserRepository;
    private final CurrentUserService currentUserService;

    public LeaderApplicationService(AppUserRepository appUserRepository, CurrentUserService currentUserService) {
        this.appUserRepository = appUserRepository;
        this.currentUserService = currentUserService;
    }

    public LeaderApplicationResponse apply() {
        AppUser currentUser = currentUserService.requireCurrentUser();

        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.LEADER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user already has posting privileges");
        }
        if (currentUser.getLeaderApplicationStatus() == LeaderApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leader application is already pending");
        }

        currentUser.setLeaderApplicationStatus(LeaderApplicationStatus.PENDING);
        currentUser.setLeaderApplicationRequestedAt(Instant.now());
        currentUser.setLeaderApplicationReviewedAt(null);
        currentUser.setLeaderApplicationReviewedBy(null);

        return toResponse(appUserRepository.save(currentUser));
    }

    public LeaderApplicationResponse getMyStatus() {
        return toResponse(currentUserService.requireCurrentUser());
    }

    public List<LeaderApplicationResponse> getPendingApplications() {
        return appUserRepository
                .findAllByLeaderApplicationStatusOrderByLeaderApplicationRequestedAtAsc(LeaderApplicationStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LeaderApplicationResponse approve(UUID userId) {
        AppUser adminUser = currentUserService.requireCurrentUser();
        AppUser targetUser = currentUserService.requireUserById(userId);

        targetUser.setRole(UserRole.LEADER);
        targetUser.setLeaderApplicationStatus(LeaderApplicationStatus.APPROVED);
        targetUser.setLeaderApplicationReviewedAt(Instant.now());
        targetUser.setLeaderApplicationReviewedBy(adminUser.getId());

        return toResponse(appUserRepository.save(targetUser));
    }

    public LeaderApplicationResponse reject(UUID userId) {
        AppUser adminUser = currentUserService.requireCurrentUser();
        AppUser targetUser = currentUserService.requireUserById(userId);

        targetUser.setLeaderApplicationStatus(LeaderApplicationStatus.REJECTED);
        targetUser.setLeaderApplicationReviewedAt(Instant.now());
        targetUser.setLeaderApplicationReviewedBy(adminUser.getId());

        return toResponse(appUserRepository.save(targetUser));
    }

    private LeaderApplicationResponse toResponse(AppUser user) {
        return LeaderApplicationResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getLeaderApplicationStatus())
                .requestedAt(user.getLeaderApplicationRequestedAt())
                .reviewedAt(user.getLeaderApplicationReviewedAt())
                .reviewedBy(user.getLeaderApplicationReviewedBy())
                .build();
    }
}
