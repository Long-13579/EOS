package com.ces.eos.controller;

import com.ces.eos.dto.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Service is healthy"));
    }
}
