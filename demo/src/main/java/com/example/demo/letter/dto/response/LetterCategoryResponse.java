package com.example.demo.letter.dto.response;

import com.example.demo.letter.domain.LetterCategory;

public record LetterCategoryResponse(
        String value,
        String label
) {
    public static LetterCategoryResponse from(LetterCategory category) {
        return new LetterCategoryResponse(category.name(), category.getLabel());
    }
}
