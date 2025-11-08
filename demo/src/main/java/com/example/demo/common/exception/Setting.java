package com.example.demo.common.exception;

public enum Setting {
    EMAIL("01092494449@hanmail.net"),
    AUTH_NUMBER("인증번호"),
    SUCCEED_SENDER_CERTIFICATION_NUMBER("인증 코드가 전송되었습니다."),
    SUCCEED_CERTIFICATION_NUMBER("인증 성공!"),
    FAIL_CERTIFICATION_NUMBER("인증번호가 틀립니다."),
    PLEASE_COMPLETE_EMAIL_VERIFICATION_FIRST("이메일 인증을 먼저 완료해주세요."),

    // ✅ Category 관련 메시지
    CATEGORY_CREATE_SUCCESS("카테고리 등록 완료"),
    CATEGORY_UPDATE_SUCCESS("카테고리 수정 완료"),
    CATEGORY_DELETE_SUCCESS("카테고리 삭제 완료"),
    FORBIDDEN_ONLY_ADMIN("관리자만 등록 가능합니다."),

    // 상품 관련 메시지
    PRODUCT_CREATE_SUCCESS("상품 등록 완료"),
    PRODUCT_UPDATE_SUCCESS("상품 수정 완료"),
    PRODUCT_DELETE_SUCCESS("상품 삭제 완료"),

    EMAIL_AVAILABLE("사용 가능한 이메일입니다."),
    EMAIL_ALREADY_EXISTS("이미 가입된 이메일입니다."),

    // Category 도메인 메시지
    CATEGORY_NAME_REQUIRED("카테고리 이름은 필수입니다."),
    CATEGORY_NAME_NOT_EMPTY("카테고리 이름은 비어 있을 수 없습니다."),
    PRODUCT_NOT_FOUND("존재하지 않는 상품입니다."),
    NOT_FOUND_CATEGORY("카테고리를 찾을 수 없습니다."),
    SET_SUBJECT("새로운 온라인 문의가 도착했습니다"),
    CONTACT_SUCCEED_MAIL("문의가 성공적으로 전송되었습니다."),
    CONTACT_FAILED_MAIL("문의 전송 중 오류가 발생했습니다."),
    CATEGORY_NOT_FOUND("존재하지 않는 카테고리입니다."),

    // 아래는 새로 추가한 상수들 ------------------------------
    AUTHENTICATED("AUTHENTICATED_"),
    BUSINESS_CHECK("사업자인증"),
    SUCCESS("인증에 성공했습니다."),
    NOT_FOUND_BUSINESS_NUMBER("없는 사업자 번호입니다."),
    PLEASE_COMPLETE_THE_BUSINESS_CERTIFICATION_FIRST("사업자 인증을 먼저 완료해주세요."),
    CORPORATION_AUTHENTICATED("CORPORATION_AUTHENTICATED_"),
    PASSWORD_CHANGE_SUCCESS("비밀번호가 성공적으로 변경되었습니다."),
    NOT_FOUND_COMPANY("회사 정보가 없습니다."),
    ADDRESS("경기도 화성시 장안면 돌서지길 132-46"),
    NEGATIVE_QUANTITY("수량은 음수가 될수 없습니다.");
    // ----------------------------------------------------



    private final String setting;

    Setting(final String setting) {
        this.setting = setting;
    }

    @Override
    public String toString() {
        return setting;
    }
}
