package com.example.demo.community.post.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.community.post.dto.response.*;
import com.example.demo.community.post.dto.request.*;
import com.example.demo.community.post.service.PostService;
import com.example.demo.login.global.annotation.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;

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

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> get(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.getById(postId)));
    }

    // ✅ 전체조회 API (getAll) 제거 (성능 문제 방지)

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllPaged(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(postService.getAllPaged(pageable)));
    }

    /** ✅ 게시글 검색 API 추가 */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> search(
            @ModelAttribute PostSearchCondition condition, // @ModelAttribute 사용
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.search(condition, pageable)));
    }
}
