package com.jugger.afc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(normalizeSecret(jwtProperties.secret()));
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.expirationMillis());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        Claims claims = parseClaims(token);
        return expectedUsername.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] normalizeSecret(String secret) {
        if (secret == null) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        if (secret.length() >= 32) {
            return secret.getBytes();
        }

        String padded = (secret + "0123456789abcdef0123456789abcdef").substring(0, 32);
        return padded.getBytes();
    }
}
