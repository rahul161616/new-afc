package com.jugger.afc.repository;

import com.jugger.afc.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<Group> findByIdAndDeletedAtIsNull(UUID id);
}
