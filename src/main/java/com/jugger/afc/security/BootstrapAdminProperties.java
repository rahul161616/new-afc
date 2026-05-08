package com.jugger.afc.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapAdminProperties(
        UUID adminId,
        String adminName,
        String adminEmail,
        String adminPassword,
        UUID memberId,
        String memberName,
        String memberEmail,
        String memberPassword
) {
}
