package com.jugger.afc.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;
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
    private List<UUID> venueIds;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer maxPlayers;
    private Integer requiredPlayers;
}
