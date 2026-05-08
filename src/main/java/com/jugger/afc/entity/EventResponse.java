package com.jugger.afc.entity;

import com.jugger.afc.enums.EventInterestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_responses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "response_status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EventInterestStatus responseStatus;

    private String note;

    @Column(name = "drop_reason")
    private String dropReason;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;

    @Column(name = "joined_waitlist_at")
    private Instant joinedWaitlistAt;

    @Column(name = "responded_at", nullable = false)
    private Instant respondedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
