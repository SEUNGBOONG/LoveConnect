package com.example.demo.match.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.Member;
import com.example.demo.match.application.MatchRequestService;
import com.example.demo.match.dto.MatchRequestCommand;
import com.example.demo.match.dto.MatchResponseDto;
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
        MatchResponseDto dto = matchService.getMatchRequest(memberId);

        if (dto == null) {
            // ❗ 요청 안한 상태 — 200 응답 + 실패 형태로 반환
            return ResponseEntity.ok(
                    ApiResponse.fail(
                            "MATCH_002",
                            "매칭 요청이 존재하지 않습니다."
                    )
            );
        }

        // 요청 존재 → 그대로 응답
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/result")
    public ResponseEntity<ApiResponse<String>> checkMatchResult(@Member Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.checkMatchResult(memberId)));
    }
}
