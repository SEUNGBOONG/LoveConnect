package com.example.demo.login.toss.controller;


import com.example.demo.login.toss.application.TossAuthService;
import com.example.demo.login.toss.dto.request.TossDisconnectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/toss")
@RequiredArgsConstructor
public class TossDisconnectController {

    private final TossAuthService tossAuthService;

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect(
            @RequestBody TossDisconnectRequest request,
            @RequestHeader("Authorization") String authorization
    ) {
        // TODO (선택): Basic Auth 검증
        tossAuthService.disconnectByCi(request.userKey());
        return ResponseEntity.ok().build(); // ★ 중요: 무조건 200 OK
    }
}
