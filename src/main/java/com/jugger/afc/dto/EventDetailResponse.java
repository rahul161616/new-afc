package com.jugger.afc.dto;

import com.jugger.afc.enums.EventInterestStatus;
import com.jugger.afc.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailResponse {
    private UUID id;
    private UUID groupId;
    private UUID venueId;
    private List<UUID> venueIds;
    private List<String> venueNames;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer requiredPlayers;
    private Integer maxPlayers;
    private EventStatus status;
    private UUID createdBy;
    private EventInterestStatus currentUserResponseStatus;
    private Integer interestedCount;
    private Integer confirmedCount;
    private Integer goingCount;
    private Integer maybeCount;
    private Integer notAvailableCount;
    private Integer waitlistedCount;
    private Integer droppedCount;
    private List<EventInterestResponse> participants;
}
