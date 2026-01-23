package com.example.demo.match.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.LoginMember;
import com.example.demo.match.application.MatchRequestService;
import com.example.demo.match.domain.MatchChannelType;
import com.example.demo.match.dto.MatchRequestCommand;
import com.example.demo.match.dto.MatchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchRequestService matchService;

    /* ==================================================
       📸 INSTAGRAM MATCH
       ================================================== */

    @PostMapping("/instagram/request")
    public ResponseEntity<ApiResponse<String>> instagramRequest(
            @RequestBody MatchRequestCommand command,
            @LoginMember Long memberId
    ) {
        matchService.createMatchRequest(
                memberId,
                command,
                MatchChannelType.INSTAGRAM
        );
        return ResponseEntity.ok(ApiResponse.success("인스타 매칭 요청 완료 ✅"));
    }

    @PutMapping("/instagram/request")
    public ResponseEntity<ApiResponse<String>> updateInstagramRequest(
            @RequestBody MatchRequestCommand command,
            @LoginMember Long memberId
    ) {
        matchService.updateMatchRequest(
                memberId,
                command,
                MatchChannelType.INSTAGRAM
        );
        return ResponseEntity.ok(ApiResponse.success("인스타 매칭 요청 수정 완료 ✏️"));
    }

    @DeleteMapping("/instagram/request")
    public ResponseEntity<ApiResponse<String>> deleteInstagramRequest(
            @LoginMember Long memberId
    ) {
        matchService.deleteMatchRequest(
                memberId,
                MatchChannelType.INSTAGRAM
        );
        return ResponseEntity.ok(ApiResponse.success("인스타 매칭 요청 삭제 완료 🗑️"));
    }

    @GetMapping("/instagram/request")
    public ResponseEntity<ApiResponse<?>> getInstagramRequest(
            @LoginMember Long memberId
    ) {
        MatchResponseDto dto =
                matchService.getMatchRequest(
                        memberId,
                        MatchChannelType.INSTAGRAM
                );

        if (dto == null) {
            return ResponseEntity.ok(
                    ApiResponse.fail(
                            "MATCH_002",
                            "인스타 매칭 요청이 존재하지 않습니다."
                    )
            );
        }

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/instagram/result")
    public ResponseEntity<ApiResponse<String>> getInstagramMatchResult(
            @LoginMember Long memberId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        matchService.checkMatchResult(
                                memberId,
                                MatchChannelType.INSTAGRAM
                        )
                )
        );
    }

    /* ==================================================
       🎵 TIKTOK MATCH
       ================================================== */

    @PostMapping("/tiktok/request")
    public ResponseEntity<ApiResponse<String>> tiktokRequest(
            @RequestBody MatchRequestCommand command,
            @LoginMember Long memberId
    ) {
        matchService.createMatchRequest(
                memberId,
                command,
                MatchChannelType.TIKTOK
        );
        return ResponseEntity.ok(ApiResponse.success("틱톡 매칭 요청 완료 ✅"));
    }

    @PutMapping("/tiktok/request")
    public ResponseEntity<ApiResponse<String>> updateTiktokRequest(
            @RequestBody MatchRequestCommand command,
            @LoginMember Long memberId
    ) {
        matchService.updateMatchRequest(
                memberId,
                command,
                MatchChannelType.TIKTOK
        );
        return ResponseEntity.ok(ApiResponse.success("틱톡 매칭 요청 수정 완료 ✏️"));
    }

    @DeleteMapping("/tiktok/request")
    public ResponseEntity<ApiResponse<String>> deleteTiktokRequest(
            @LoginMember Long memberId
    ) {
        matchService.deleteMatchRequest(
                memberId,
                MatchChannelType.TIKTOK
        );
        return ResponseEntity.ok(ApiResponse.success("틱톡 매칭 요청 삭제 완료 🗑️"));
    }

    @GetMapping("/tiktok/request")
    public ResponseEntity<ApiResponse<?>> getTiktokRequest(
            @LoginMember Long memberId
    ) {
        MatchResponseDto dto =
                matchService.getMatchRequest(
                        memberId,
                        MatchChannelType.TIKTOK
                );

        if (dto == null) {
            return ResponseEntity.ok(
                    ApiResponse.fail(
                            "MATCH_002",
                            "틱톡 매칭 요청이 존재하지 않습니다."
                    )
            );
        }

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/tiktok/result")
    public ResponseEntity<ApiResponse<String>> getTiktokMatchResult(
            @LoginMember Long memberId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        matchService.checkMatchResult(
                                memberId,
                                MatchChannelType.TIKTOK
                        )
                )
        );
    }
}
