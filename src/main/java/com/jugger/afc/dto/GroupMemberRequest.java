package com.jugger.afc.dto;

import com.jugger.afc.enums.GroupMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberRequest {
    private UUID userId;
    private GroupMemberRole role;
}
