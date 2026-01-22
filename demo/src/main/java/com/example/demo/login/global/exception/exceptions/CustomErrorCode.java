package com.example.demo.login.global.exception.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomErrorCode {

    // 🔐 Token 관련
    NOT_FIND_TOKEN(HttpStatus.UNAUTHORIZED, "T001", "토큰을 찾을 수 없습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "T002", "토큰이 만료됐습니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "PHONE_003", "이미 가입된 전화번호입니다."),
    // 🛒 장바구니 예시
    CART_ITEM_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CART_001", "이미 장바구니에 담긴 상품입니다."),
    CART_NOT_FOUND_ITEM(HttpStatus.BAD_REQUEST, "CART_002", "장바구니 아이템이 없습니다."),
    NOT_FOUND_DETAIL(HttpStatus.BAD_REQUEST, "DETAIL_001", "상세 정보가 없습니다."),
    // 기존 에러코드 아래에 추가
    POST_UNAUTHORIZED(HttpStatus.FORBIDDEN, "POST_002", "게시글 작성자가 아닙니다."),

    // 💘 매칭
    MATCH_ALREADY_REQUESTED(HttpStatus.BAD_REQUEST, "MATCH_001", "이미 한 명에게 요청을 보냈습니다."),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_002", "매칭 요청이 존재하지 않습니다."),
    MATCH_ALREADY_COMPLETED(HttpStatus.CONFLICT, "MATCH_003", "이미 매칭된 요청은 수정/삭제할 수 없습니다."),
    MATCH_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_004", "상대방 매칭 요청이 없습니다."),
    MATCH_MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "MATCH_005", "회원이 존재하지 않습니다."),
    MATCH_RESULT_PENDING(HttpStatus.OK, "MATCH_006", "아직 상대방이 요청하지 않았습니다."),
    DUPLICATE_MATCH_REQUEST(HttpStatus.BAD_REQUEST, "MATCH_007", "중복 매칭 요청입니다."),

    // 👤 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원 정보를 찾을 수 없습니다."),

    // 📝 게시글
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다."),

    // 💬 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_001", "댓글을 찾을 수 없습니다."),
    COMMENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "COMMENT_002", "댓글 작성자가 아닙니다."),

    // 🔐 인증 / 회원가입
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "AUTH_001", "이미 사용중인 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_002", "이미 사용중인 이메일입니다."),
    NOT_SAME_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_003", "비밀번호가 일치하지 않습니다."),
    PHONE_AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_004", "전화번호 인증이 필요합니다."),
    MEMBER_WITHDRAWN(HttpStatus.UNAUTHORIZED, "MEMBER_002", "탈퇴한 회원입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_001", "찾을 수가 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;

    CustomErrorCode(HttpStatus httpStatus, String customCode, String message) {
        this.httpStatus = httpStatus;
        this.customCode = customCode;
        this.message = message;
    }
}
