package com.jugger.afc.repository;

import com.jugger.afc.entity.AppUser;
import com.jugger.afc.enums.LeaderApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmailAndDeletedAtIsNull(String email);

    List<AppUser> findAllByLeaderApplicationStatusOrderByLeaderApplicationRequestedAtAsc(
            LeaderApplicationStatus leaderApplicationStatus
    );
}
