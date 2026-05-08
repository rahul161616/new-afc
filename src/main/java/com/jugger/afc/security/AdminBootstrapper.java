package com.jugger.afc.security;

import com.jugger.afc.entity.AppUser;
import com.jugger.afc.enums.LeaderApplicationStatus;
import com.jugger.afc.enums.UserRole;
import com.jugger.afc.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AdminBootstrapper implements CommandLineRunner {
    private final BootstrapAdminProperties bootstrapAdminProperties;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapper(
            BootstrapAdminProperties bootstrapAdminProperties,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.bootstrapAdminProperties = bootstrapAdminProperties;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Instant now = Instant.now();
        upsertAdmin(now);
        upsertDemoMember(now);
    }

    private void upsertAdmin(Instant now) {
        AppUser adminUser = appUserRepository.findById(bootstrapAdminProperties.adminId())
                .orElseGet(() -> AppUser.builder()
                        .id(bootstrapAdminProperties.adminId())
                        .createdAt(now)
                        .build());

        adminUser.setName(bootstrapAdminProperties.adminName());
        adminUser.setEmail(bootstrapAdminProperties.adminEmail());
        adminUser.setPasswordHash(passwordEncoder.encode(bootstrapAdminProperties.adminPassword()));
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setLeaderApplicationStatus(LeaderApplicationStatus.APPROVED);
        adminUser.setDeletedAt(null);
        adminUser.setUpdatedAt(now);

        if (adminUser.getCreatedAt() == null) {
            adminUser.setCreatedAt(now);
        }

        appUserRepository.save(adminUser);
    }

    private void upsertDemoMember(Instant now) {
        AppUser memberUser = appUserRepository.findById(bootstrapAdminProperties.memberId())
                .orElseGet(() -> AppUser.builder()
                        .id(bootstrapAdminProperties.memberId())
                        .createdAt(now)
                        .build());

        memberUser.setName(bootstrapAdminProperties.memberName());
        memberUser.setEmail(bootstrapAdminProperties.memberEmail());
        memberUser.setPasswordHash(passwordEncoder.encode(bootstrapAdminProperties.memberPassword()));
        memberUser.setRole(UserRole.MEMBER);
        memberUser.setLeaderApplicationStatus(LeaderApplicationStatus.NONE);
        memberUser.setDeletedAt(null);
        memberUser.setUpdatedAt(now);
        memberUser.setLeaderApplicationRequestedAt(null);
        memberUser.setLeaderApplicationReviewedAt(null);
        memberUser.setLeaderApplicationReviewedBy(null);

        if (memberUser.getCreatedAt() == null) {
            memberUser.setCreatedAt(now);
        }

        appUserRepository.save(memberUser);
    }
}
