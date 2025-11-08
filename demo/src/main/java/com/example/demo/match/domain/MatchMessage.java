package com.example.demo.match.domain;

import lombok.Getter;

@Getter
public enum MatchMessage {
    PERFECT_MATCH("🌟 완벽한 궁합이에요! 재회 가능성 매우 높아요."),
    GREAT_MATCH("👍 좋은 궁합이에요! 가능성이 꽤 있어요."),
    GOOD_MATCH("🙂 시도해볼 만해요."),
    AVERAGE_MATCH("😐 조심스럽게 접근해보세요."),
    LOW_MATCH("❌ 낮은 매칭 점수입니다."),
    HIGH_DESIRE_GAP("⚠ 재회 의지 차이가 너무 커요.");

    private final String message;

    MatchMessage(String message) {
        this.message = message;
    }
}
