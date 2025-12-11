package com.example.demo.breakup.dto;

import com.example.demo.breakup.domain.BreakupReasonType;

import java.util.List;

public record BreakupReasonRequest(
        List<BreakupReasonType> reasons,
        String etcReason // 기타 사유 입력란 (선택적)
) {
}
