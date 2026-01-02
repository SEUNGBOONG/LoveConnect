package com.example.demo.match.domain;

import lombok.Getter;

@Getter
public enum MatchMessage {
    PERFECT_MATCH("매칭 완료되었습니다. 서로를 기다리고 있어요."),
    GREAT_MATCH("매칭 완료되었습니다. 서로를 기다리고 있어요."),
    GOOD_MATCH("매칭 완료되었습니다. 서로를 기다리고 있어요."),
    AVERAGE_MATCH("매칭 완료되었습니다. 서로를 기다리고 있어요."),
    LOW_MATCH("매칭 완료되었습니다. 서로를 기다리고 있어요."),
    HIGH_DESIRE_GAP("매칭 완료되었습니다. 서로를 기다리고 있어요."); // 의지 차이가 커도 멘트는 동일하게 설정

    private final String message;

    MatchMessage(String message) {
        this.message = message;
    }
}
