package com.example.demo.letter.controller;

import com.example.demo.common.exception.ApiResponse;
import com.example.demo.letter.dto.request.LetterCreateRequest;
import com.example.demo.letter.dto.request.LetterUpdateRequest;
import com.example.demo.letter.domain.LetterCategory;
import com.example.demo.letter.dto.response.LetterCategoryResponse;
import com.example.demo.letter.dto.response.LetterPageResponse;
import com.example.demo.letter.dto.response.LetterResponse;
import com.example.demo.letter.service.LetterService;
import com.example.demo.login.global.annotation.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/letters")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @PostMapping
    public ResponseEntity<ApiResponse<LetterResponse>> create(
            @Member Long memberId,
            @Valid @RequestBody LetterCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(letterService.create(memberId, request)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<LetterCategoryResponse>>> getCategories() {
        List<LetterCategoryResponse> categories = Arrays.stream(LetterCategory.values())
                .map(LetterCategoryResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<LetterPageResponse>> getMine(
            @Member Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(LetterPageResponse.from(letterService.getMine(memberId, pageable))));
    }

    @GetMapping("/{letterId}")
    public ResponseEntity<ApiResponse<LetterResponse>> get(
            @Member Long memberId,
            @PathVariable Long letterId
    ) {
        return ResponseEntity.ok(ApiResponse.success(letterService.get(memberId, letterId)));
    }

    @PutMapping("/{letterId}")
    public ResponseEntity<ApiResponse<LetterResponse>> update(
            @Member Long memberId,
            @PathVariable Long letterId,
            @Valid @RequestBody LetterUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(letterService.update(memberId, letterId, request)));
    }

    @DeleteMapping("/{letterId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Member Long memberId,
            @PathVariable Long letterId
    ) {
        letterService.delete(memberId, letterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
