package com.example.demo.match.strategy;

import com.example.demo.match.domain.MatchMessage;

public class MatchScoreStrategy {

    public static MatchMessage calculate(int requesterDesire, int targetDesire) {
        double average = (requesterDesire + targetDesire) / 2.0;
        int gap = Math.abs(requesterDesire - targetDesire);

        if (gap >= 7) return MatchMessage.HIGH_DESIRE_GAP;
        if (average >= 9) return MatchMessage.PERFECT_MATCH;
        if (average >= 8) return MatchMessage.GREAT_MATCH;
        if (average >= 6) return MatchMessage.GOOD_MATCH;
        if (average >= 4) return MatchMessage.AVERAGE_MATCH;
        return MatchMessage.LOW_MATCH;
    }
}
