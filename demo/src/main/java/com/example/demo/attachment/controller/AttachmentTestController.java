package com.example.demo.attachment.controller;

import com.example.demo.attachment.domain.entity.AttachmentQuestion;
import com.example.demo.attachment.dto.request.AttachmentSubmitRequest;
import com.example.demo.attachment.dto.response.AttachmentResultResponse;
import com.example.demo.attachment.service.AttachmentTestService;
import com.example.demo.common.exception.ApiResponse;
import com.example.demo.login.global.annotation.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attachment-test")
@RequiredArgsConstructor
public class AttachmentTestController {

    private final AttachmentTestService attachmentTestService;

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<AttachmentQuestion>>> getQuestions() {
        return ResponseEntity.ok(ApiResponse.success(attachmentTestService.getQuestions()));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<AttachmentResultResponse>> submit(
            @LoginMember Long memberId,
            @RequestBody AttachmentSubmitRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        attachmentTestService.evaluate(memberId, request)
                )
        );
    }

    @GetMapping("/result")
    public ResponseEntity<ApiResponse<List<AttachmentResultResponse>>> getResults(
            @LoginMember Long memberId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(attachmentTestService.getResultHistory(memberId))
        );
    }
}
