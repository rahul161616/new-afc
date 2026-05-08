package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.AuthLoginRequest;
import com.jugger.afc.dto.AuthResponse;
import com.jugger.afc.dto.AuthSignupRequest;
import com.jugger.afc.dto.GoogleAuthRequest;
import com.jugger.afc.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.AUTH)
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthSignupRequest request) {
        return ResponseEntity.status(201).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.google(request));
    }
}
