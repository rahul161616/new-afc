package com.jugger.afc.repository;

import com.jugger.afc.entity.GroupMember;
import com.jugger.afc.enums.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findAllByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(UUID groupId);

    List<GroupMember> findAllByGroupIdAndStatusOrderByJoinedAtAsc(UUID groupId, GroupMemberStatus status);

    long countByGroupIdAndStatus(UUID groupId, GroupMemberStatus status);

    List<GroupMember> findAllByUserIdAndIsActiveTrue(UUID userId);

    List<GroupMember> findAllByUserId(UUID userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserIdAndIsActiveTrue(UUID groupId, UUID userId);
}
