package com.jugger.afc.dto;

import com.jugger.afc.enums.EventInterestStatus;
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
public class EventInterestResponse {
    private UUID id;
    private UUID eventId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private EventInterestStatus status;
    private String note;
    private String dropReason;
    private Integer waitlistPosition;
    private Instant respondedAt;
}
