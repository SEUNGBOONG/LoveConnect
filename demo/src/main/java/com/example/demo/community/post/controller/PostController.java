package com.example.demo.community.post.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.community.post.dto.request.PostCreateRequest;
import com.example.demo.community.post.dto.request.PostSearchCondition;
import com.example.demo.community.post.dto.request.PostUpdateRequest;
import com.example.demo.community.post.dto.response.PostPageResponse;
import com.example.demo.community.post.dto.response.PostResponse;
import com.example.demo.community.post.service.PostService;
import com.example.demo.login.global.annotation.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(@Member Long memberId, @RequestBody PostCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postService.create(memberId, request)));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> update(@Member Long memberId, @PathVariable Long postId, @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postService.update(memberId, postId, request)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> delete(@Member Long memberId, @PathVariable Long postId) {
        postService.delete(memberId, postId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> get(@Member Long memberId, @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.getById(memberId, postId)));
    }

    /** ✅ 전체 페이징 조회 */
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PostPageResponse>> getAllPaged(
            @Member Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(PostPageResponse.from(postService.getAllPaged(memberId, pageable))));
    }

    /** ✅ 검색 페이징 조회 */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PostPageResponse>> search(
            @ModelAttribute PostSearchCondition condition,
            @Member Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // service에서 이미 PostResponse로 변환해서 오므로 바로 PostPageResponse.from 사용
        return ResponseEntity.ok(ApiResponse.success(PostPageResponse.from(postService.search(condition, memberId, pageable))));
    }
}
