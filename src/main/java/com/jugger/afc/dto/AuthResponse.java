package com.jugger.afc.dto;

import com.jugger.afc.enums.UserRole;
import com.jugger.afc.enums.LeaderApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private UserProfileResponse user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileResponse {
        private UUID id;
        private String name;
        private String email;
        private String phone;
        private UserRole role;
        private LeaderApplicationStatus leaderApplicationStatus;
    }
}
