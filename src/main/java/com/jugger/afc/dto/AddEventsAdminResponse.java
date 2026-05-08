package com.jugger.afc.dto;

import com.jugger.afc.enums.EventStatus;
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
public class AddEventsAdminResponse {
    private UUID id;
    private UUID groupId;
    private UUID venueId;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer requiredPlayers;
    private Integer maxPlayers;
    private EventStatus status;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
