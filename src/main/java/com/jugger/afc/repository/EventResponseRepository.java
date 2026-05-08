package com.jugger.afc.repository;

import com.jugger.afc.entity.EventResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventResponseRepository extends JpaRepository<EventResponse, UUID> {
    Optional<EventResponse> findByEventIdAndUserId(UUID eventId, UUID userId);

    List<EventResponse> findAllByEventIdOrderByRespondedAtAsc(UUID eventId);

    List<EventResponse> findAllByEventIdIn(Collection<UUID> eventIds);

    long countByEventIdAndResponseStatusIn(UUID eventId, Collection<com.jugger.afc.enums.EventInterestStatus> statuses);
}
