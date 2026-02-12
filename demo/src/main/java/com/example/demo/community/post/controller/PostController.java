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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** ✅ 게시글 작성 */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @Member Long memberId,
            @RequestBody PostCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.create(memberId, request)));
    }

    /** ✅ 게시글 수정 */
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @Member Long memberId,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.update(memberId, postId, request)));
    }

    /** ✅ 게시글 삭제 */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Member Long memberId,
            @PathVariable Long postId
    ) {
        postService.delete(memberId, postId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** ✅ 게시글 상세조회 */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> get(
            @Member Long memberId,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getById(memberId, postId)));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PostPageResponse>> getAllPaged(

            @Member Long memberId,
            Pageable pageable
    ) {
        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var page = postService.getAllPaged(memberId, fixedPageable);

        return ResponseEntity.ok(ApiResponse.success(PostPageResponse.from(page)));
    }

    /** ✅ 검색 */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> search(
            @ModelAttribute PostSearchCondition condition,
            @Member Long memberId,
            Pageable pageable
    ) {

        Pageable fixedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                ApiResponse.success(postService.search(condition, memberId, fixedPageable))
        );
    }
}
