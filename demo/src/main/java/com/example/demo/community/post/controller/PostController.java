package com.example.demo.community.post.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.community.post.dto.request.*;
import com.example.demo.community.post.dto.response.PostResponse;
import com.example.demo.community.post.service.PostService;
import com.example.demo.login.global.annotation.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @Member Long memberId,
            @RequestBody PostCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.create(memberId, request)));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @Member Long memberId,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.update(memberId, postId, request)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Member Long memberId,
            @PathVariable Long postId
    ) {
        postService.delete(memberId, postId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** ✅ 상세조회 (isMine 포함) */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> get(
            @Member Long memberId,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getById(memberId, postId)));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllPaged(
            @Member Long memberId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getAllPaged(memberId, pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> search(
            @ModelAttribute PostSearchCondition condition,
            @Member Long memberId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.search(condition, memberId, pageable)));
    }
}
