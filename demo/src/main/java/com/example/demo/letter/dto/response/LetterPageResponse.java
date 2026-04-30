package com.example.demo.letter.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record LetterPageResponse(
        List<LetterResponse> content,
        long totalElements,
        int totalPages,
        int pageNumber,
        int pageSize,
        boolean first,
        boolean last,
        int numberOfElements,
        boolean empty
) {
    public static LetterPageResponse from(Page<LetterResponse> page) {
        return new LetterPageResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements(),
                page.isEmpty()
        );
    }
}
