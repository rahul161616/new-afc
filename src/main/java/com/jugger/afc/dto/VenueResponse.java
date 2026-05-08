package com.jugger.afc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private UUID id;
    private UUID groupId;
    private String name;
    private String address;
    private String mapUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
