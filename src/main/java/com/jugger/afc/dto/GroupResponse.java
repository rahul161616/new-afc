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
public class GroupResponse {
    private UUID id;
    private String name;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer activeMemberCount;
    private GroupMemberRole currentUserRole;
    private GroupMemberStatus currentUserStatus;
}
