package com.example.demo.match.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.Member;
import com.example.demo.match.application.MatchRequestService;
import com.example.demo.match.dto.MatchRequestCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchRequestService matchService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<String>> requestMatch(
            @RequestBody MatchRequestCommand command,
            @Member Long memberId
    ) {
        matchService.createMatchRequest(memberId, command);
        return ResponseEntity.ok(ApiResponse.success("매칭 요청 완료 ✅"));
    }

    @PutMapping("/request")
    public ResponseEntity<ApiResponse<String>> updateMatchRequest(
            @RequestBody MatchRequestCommand command,
            @Member Long memberId
    ) {
        matchService.updateMatchRequest(memberId, command);
        return ResponseEntity.ok(ApiResponse.success("매칭 요청 수정 완료 ✏️"));
    }

    @DeleteMapping("/request")
    public ResponseEntity<ApiResponse<String>> deleteMatchRequest(@Member Long memberId) {
        matchService.deleteMatchRequest(memberId);
        return ResponseEntity.ok(ApiResponse.success("매칭 요청 삭제 완료 🗑️"));
    }

    @GetMapping("/request")
    public ResponseEntity<ApiResponse<?>> getMatchRequest(@Member Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getMatchRequest(memberId)));
    }

    @GetMapping("/result")
    public ResponseEntity<ApiResponse<String>> checkMatchResult(@Member Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.checkMatchResult(memberId)));
    }
}
