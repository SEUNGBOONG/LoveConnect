package com.example.demo.login.global.exception.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomErrorCode {

    // ✅ 토큰 관련
    NOT_FIND_TOKEN(HttpStatus.UNAUTHORIZED, "T001", "토큰을 찾을 수 없습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "T002", "토큰 시간이 만료됐습니다."),

    // ✅ 장바구니 예시 (기존)
    CART_ITEM_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CART_001","이미 장바구니에 담긴 상품입니다."),
    NOT_FOUND_DETAIL(HttpStatus.BAD_REQUEST,"DETAIL_001", "상세 정보가 없습니다."),
    CART_NOT_FOUND_ITEM(HttpStatus.BAD_REQUEST,"CART_002","장바구니 아이템이 없습니다."),

    // ✅ 💘 매칭 관련
    MATCH_ALREADY_REQUESTED(HttpStatus.BAD_REQUEST, "MATCH_001", "이미 한 명에게 요청을 보냈습니다."),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_002", "매칭 요청이 존재하지 않습니다."),
    MATCH_ALREADY_COMPLETED(HttpStatus.CONFLICT, "MATCH_003", "이미 매칭된 요청은 수정/삭제할 수 없습니다."),
    MATCH_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_004", "상대방 매칭 요청이 없습니다."),
    MATCH_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_005", "회원을 찾을 수 없습니다."),
    MATCH_RESULT_PENDING(HttpStatus.OK, "MATCH_006", "아직 상대방이 요청하지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;

    CustomErrorCode(HttpStatus httpStatus, String customCode, String message) {
        this.httpStatus = httpStatus;
        this.customCode = customCode;
        this.message = message;
    }
}
