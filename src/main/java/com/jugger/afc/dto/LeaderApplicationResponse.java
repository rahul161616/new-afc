package com.jugger.afc.dto;

import com.jugger.afc.enums.LeaderApplicationStatus;
import com.jugger.afc.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderApplicationResponse {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private LeaderApplicationStatus status;
    private Instant requestedAt;
    private Instant reviewedAt;
    private UUID reviewedBy;
}
