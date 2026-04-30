package com.example.demo.letter.dto.response;

import com.example.demo.letter.domain.Letter;
import com.example.demo.letter.domain.LetterCategory;

import java.time.LocalDateTime;

public record LetterResponse(
        Long id,
        String category,
        String categoryLabel,
        String title,
        String content,
        String shareStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LetterResponse from(Letter letter) {
        LetterCategory category = letter.getCategory();

        return new LetterResponse(
                letter.getId(),
                category.name(),
                category.getLabel(),
                letter.getTitle(),
                letter.getContent(),
                letter.getShareStatus().name(),
                letter.getCreatedAt(),
                letter.getUpdatedAt()
        );
    }
}
