package com.jugger.afc.entity;

import com.jugger.afc.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "futsal_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FutsalEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private String title;
    private String description;

    @Column(name = "venue_id", nullable = false)
    private UUID venueId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "futsal_event_venues",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "venue_id")
    )
    @Builder.Default
    private Set<Venue> venues = new LinkedHashSet<>();

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers;

    @Column(name = "required_players", nullable = false)
    private Integer requiredPlayers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
