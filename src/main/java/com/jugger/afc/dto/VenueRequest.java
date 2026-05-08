package com.jugger.afc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueRequest {
    private UUID groupId;
    private String name;
    private String address;
    private String mapUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isActive;
}
