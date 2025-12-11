package com.example.demo.breakup.controller;

import com.example.demo.breakup.dto.BreakupReasonRequest;
import com.example.demo.breakup.service.BreakupReasonService;
import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/breakup")
@RequiredArgsConstructor
public class BreakupReasonController {

    private final BreakupReasonService breakupReasonService;

    @PostMapping("/reason")
    public ResponseEntity<ApiResponse<String>> submitBreakupReason(
            @Member Long memberId,
            @RequestBody BreakupReasonRequest request
    ) {
        breakupReasonService.saveReason(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("이별 이유 저장 완료"));
    }
}
