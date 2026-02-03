package com.example.demo.match.domain.value;

import com.example.demo.common.util.AESUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumber {

    @Column(name = "phone_number", nullable = false)
    private String encrypted;

    private PhoneNumber(String encrypted) {
        this.encrypted = encrypted;
    }

    public static PhoneNumber fromRaw(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        return new PhoneNumber(AESUtil.encrypt(raw.trim()));
    }

    public static PhoneNumber fromEncrypted(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            throw new IllegalArgumentException("암호화된 전화번호는 필수입니다.");
        }
        return new PhoneNumber(encrypted.trim());
    }

    public String decrypt() {
        return AESUtil.decrypt(encrypted);
    }
}
