package com.jugger.afc.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AddEventsAdminRequest {
    private UUID groupId;
    private UUID venueId;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer maxPlayers;
    private Integer requiredPlayers;
}
