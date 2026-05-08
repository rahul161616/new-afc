package com.jugger.afc.service;

import com.jugger.afc.entity.AppUser;
import com.jugger.afc.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class CurrentUserService {
    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return appUserRepository.findByEmailAndDeletedAtIsNull(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    public Optional<AppUser> findCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if ("anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return Optional.empty();
        }

        return appUserRepository.findByEmailAndDeletedAtIsNull(authentication.getName());
    }

    public AppUser requireUserById(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
