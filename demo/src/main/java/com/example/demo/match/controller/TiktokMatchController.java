package com.example.demo.match.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.Member;
import com.example.demo.match.application.TiktokMatchRequestService;
import com.example.demo.match.dto.TiktokMatchRequestCommand;
import com.example.demo.match.dto.TiktokMatchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches/tiktok")
@RequiredArgsConstructor
public class TiktokMatchController {

    private final TiktokMatchRequestService matchService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<String>> requestMatch(
            @RequestBody TiktokMatchRequestCommand command,
            @Member Long memberId
    ) {
        matchService.createMatchRequest(memberId, command);
        return ResponseEntity.ok(ApiResponse.success("틱톡 매칭 요청 완료 ✅"));
    }

    @PutMapping("/request")
    public ResponseEntity<ApiResponse<String>> updateMatchRequest(
            @RequestBody TiktokMatchRequestCommand command,
            @Member Long memberId
    ) {
        matchService.updateMatchRequest(memberId, command);
        return ResponseEntity.ok(ApiResponse.success("틱톡 매칭 요청 수정 완료 ✏️"));
    }

    @DeleteMapping("/request")
    public ResponseEntity<ApiResponse<String>> deleteMatchRequest(@Member Long memberId) {
        matchService.deleteMatchRequest(memberId);
        return ResponseEntity.ok(ApiResponse.success("틱톡 매칭 요청 삭제 완료 🗑️"));
    }

    @GetMapping("/request")
    public ResponseEntity<ApiResponse<?>> getMatchRequest(@Member Long memberId) {
        TiktokMatchResponseDto dto = matchService.getMatchRequest(memberId);

        if (dto == null) {
            return ResponseEntity.ok(
                    ApiResponse.fail("MATCH_002", "틱톡 매칭 요청이 존재하지 않습니다.")
            );
        }

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
