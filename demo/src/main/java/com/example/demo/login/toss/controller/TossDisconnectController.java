package com.example.demo.login.toss.controller;

import com.example.demo.login.toss.application.TossAuthService;
import com.example.demo.login.toss.dto.request.TossDisconnectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/toss")
@RequiredArgsConstructor
public class TossDisconnectController {

    private final TossAuthService tossAuthService;

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect(
            @RequestBody TossDisconnectRequest request) {

        tossAuthService.disconnectByUserKey(request.userKey());
        return ResponseEntity.ok().build();
    }
}
