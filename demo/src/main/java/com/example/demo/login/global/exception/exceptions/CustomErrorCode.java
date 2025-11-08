package com.example.demo.login.global.exception.exceptions;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomErrorCode {

    NOT_FIND_TOKEN(HttpStatus.UNAUTHORIZED, "T001", "토큰을 찾을 수 없습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "T002", "토큰 시간이 만료됐습니다."),
    CART_ITEM_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CART_001","이미 장바구니에 담긴 상품입니다."),
    NOT_FOUND_DETAIL(HttpStatus.BAD_REQUEST,"DETAIL_001", "상세 정보가 없습니다."),
    CART_NOT_FOUND_ITEM(HttpStatus.BAD_REQUEST,"CART_OO2","장바구니 아이템이 없습니다.");
    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;

    CustomErrorCode(HttpStatus httpStatus, String customCode, String message) {
        this.httpStatus = httpStatus;
        this.customCode = customCode;
        this.message = message;
    }
}
