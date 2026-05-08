package com.jugger.afc.dto;

import com.jugger.afc.enums.EventInterestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInterestRequest {
    private EventInterestStatus status;
    private String note;
    private String dropReason;
}
