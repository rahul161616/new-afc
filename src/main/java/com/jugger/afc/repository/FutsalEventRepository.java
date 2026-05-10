package com.jugger.afc.repository;

import com.jugger.afc.entity.FutsalEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FutsalEventRepository extends JpaRepository<FutsalEvent, UUID> {
    @EntityGraph(attributePaths = "venues")
    List<FutsalEvent> findAllByDeletedAtIsNullOrderByStartTimeAsc();

    @EntityGraph(attributePaths = "venues")
    Optional<FutsalEvent> findByIdAndDeletedAtIsNull(UUID id);
}
