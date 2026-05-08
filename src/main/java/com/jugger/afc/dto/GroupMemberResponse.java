package com.jugger.afc.dto;

import com.jugger.afc.enums.GroupMemberRole;
import com.jugger.afc.enums.GroupMemberStatus;
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
public class GroupMemberResponse {
    private UUID id;
    private UUID groupId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private GroupMemberRole role;
    private GroupMemberStatus status;
    private Instant joinedAt;
    private Boolean isActive;
}
