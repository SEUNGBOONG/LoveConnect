package com.example.demo.community.comment.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.community.comment.dto.request.CommentCreateRequest;
import com.example.demo.community.comment.dto.request.CommentUpdateRequest;
import com.example.demo.community.comment.dto.response.CommentResponse;
import com.example.demo.community.comment.service.CommentService;
import com.example.demo.login.global.annotation.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** ✅ 댓글 작성 */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @Member Long memberId,
            @RequestBody CommentCreateRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(commentService.create(memberId, request)));
    }

    /** ✅ 댓글 수정 */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @Member Long memberId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(commentService.update(memberId, commentId, request)));
    }

    /** ✅ 댓글 삭제 */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Member Long memberId,
            @PathVariable Long commentId
    ) {
        commentService.delete(memberId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** ✅ 게시글별 댓글 조회 */
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getByPost(postId)));
    }

    /** ✅ 게시글별 댓글 페이징 조회 */
    @GetMapping("/post/{postId}/paged")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getByPostPaged(
            @PathVariable Long postId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getByPostPaged(postId, pageable)));
    }
}
