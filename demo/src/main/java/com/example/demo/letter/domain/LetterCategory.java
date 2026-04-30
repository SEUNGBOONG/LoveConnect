package com.example.demo.letter.domain;

import java.util.Arrays;

public enum LetterCategory {
    REUNION_REASON("재회하고 싶은 이유"),
    APOLOGY("사과하고 싶은 말"),
    PROMISE("다시 만나면 지키고 싶은 약속"),
    UNSENT_MESSAGE("아직 보내면 안 되는 메시지"),
    EMOTION_NOTE("오늘의 감정 기록"),
    ETC("기타");

    private final String label;

    LetterCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static LetterCategory from(String value) {
        if (value == null || value.isBlank()) {
            return ETC;
        }

        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 편지 카테고리입니다."));
    }
}
