package com.jugger.afc.controller;

import com.jugger.afc.ApiConstants;
import com.jugger.afc.dto.AddEventsAdminRequest;
import com.jugger.afc.dto.AddEventsAdminResponse;
import com.jugger.afc.service.AddEventsByAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.ADMIN_EVENTS)
@RequiredArgsConstructor
public class AddEventsAdminController {
    private final AddEventsByAdminService addEventsAdminService;

    @PostMapping
    public ResponseEntity<AddEventsAdminResponse> addEventsbyAdmin(@RequestBody AddEventsAdminRequest addEventsAdminRequest){
        AddEventsAdminResponse response = addEventsAdminService.addEventsByAdmin(addEventsAdminRequest);
        return ResponseEntity.status(201).body(response);
    }
}
