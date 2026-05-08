package com.jugger.afc.service;

import com.jugger.afc.dto.AuthLoginRequest;
import com.jugger.afc.dto.AuthResponse;
import com.jugger.afc.dto.AuthSignupRequest;
import com.jugger.afc.dto.GoogleAuthRequest;
import com.jugger.afc.entity.AppUser;
import com.jugger.afc.enums.LeaderApplicationStatus;
import com.jugger.afc.enums.UserRole;
import com.jugger.afc.repository.AppUserRepository;
import com.jugger.afc.security.JwtProperties;
import com.jugger.afc.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final String googleClientId;
    private final JwtDecoder googleJwtDecoder;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.googleClientId = googleClientId;
        this.googleJwtDecoder = buildGoogleJwtDecoder(googleClientId);
    }

    public AuthResponse signup(AuthSignupRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signup request cannot be null");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be blank");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (appUserRepository.findByEmailAndDeletedAtIsNull(request.getEmail().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        Instant now = Instant.now();
        AppUser appUser = AppUser.builder()
                .id(UUID.randomUUID())
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() == null ? null : request.getPhone().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.MEMBER)
                .leaderApplicationStatus(LeaderApplicationStatus.NONE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return buildAuthResponse(appUserRepository.save(appUser));
    }

    public AuthResponse login(AuthLoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        AppUser appUser = appUserRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        return buildAuthResponse(appUser);
    }

    public AuthResponse google(GoogleAuthRequest request) {
        if (request == null || request.getCredential() == null || request.getCredential().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google credential is required");
        }

        Jwt googleToken = decodeGoogleToken(request.getCredential());
        String email = googleToken.getClaimAsString("email");
        Boolean emailVerified = googleToken.getClaimAsBoolean("email_verified");

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account email is required");
        }
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
        }

        String normalizedEmail = email.trim().toLowerCase();
        AppUser appUser = appUserRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseGet(() -> createGoogleUser(googleToken, normalizedEmail));

        return buildAuthResponse(appUser);
    }

    private AuthResponse buildAuthResponse(AppUser appUser) {
        String token = jwtService.generateToken(
                appUser.getEmail(),
                Map.of(
                        "role", appUser.getRole().name(),
                        "userId", appUser.getId().toString(),
                        "name", appUser.getName()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.expirationMillis() / 1000)
                .user(AuthResponse.UserProfileResponse.builder()
                        .id(appUser.getId())
                        .name(appUser.getName())
                        .email(appUser.getEmail())
                        .phone(appUser.getPhone())
                        .role(appUser.getRole())
                        .leaderApplicationStatus(appUser.getLeaderApplicationStatus())
                        .build())
                .build();
    }

    private Jwt decodeGoogleToken(String credential) {
        try {
            return googleJwtDecoder.decode(credential);
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google credential");
        }
    }

    private AppUser createGoogleUser(Jwt googleToken, String normalizedEmail) {
        Instant now = Instant.now();
        String name = googleToken.getClaimAsString("name");
        if (name == null || name.isBlank()) {
            name = normalizedEmail.substring(0, normalizedEmail.indexOf("@"));
        }

        AppUser appUser = AppUser.builder()
                .id(UUID.randomUUID())
                .name(name.trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(UserRole.MEMBER)
                .leaderApplicationStatus(LeaderApplicationStatus.NONE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return appUserRepository.save(appUser);
    }

    private JwtDecoder buildGoogleJwtDecoder(String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
        OAuth2TokenValidator<Jwt> withIssuer = new JwtClaimValidator<>(
                "iss",
                issuer -> "https://accounts.google.com".equals(issuer) || "accounts.google.com".equals(issuer)
        );
        OAuth2TokenValidator<Jwt> withAudience = new JwtClaimValidator<List<String>>(
                "aud",
                audience -> audience != null && audience.contains(clientId)
        );
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                withIssuer,
                withAudience,
                token -> token.getSubject() == null || token.getSubject().isBlank()
                        ? OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Subject is required", null))
                        : OAuth2TokenValidatorResult.success()
        );
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
