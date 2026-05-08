package com.jugger.afc.repository;

import com.jugger.afc.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
    List<Venue> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<Venue> findByIdAndDeletedAtIsNull(UUID id);
}
