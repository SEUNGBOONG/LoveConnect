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
public class SocialId {

    @Column(name = "social_id", nullable = false)
    private String encrypted;

    private SocialId(String encrypted) {
        this.encrypted = encrypted;
    }

    public static SocialId fromRaw(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("소셜 ID는 필수입니다.");
        }
        return new SocialId(
                AESUtil.encrypt(raw.trim().toLowerCase())
        );
    }

    public static SocialId fromEncrypted(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            throw new IllegalArgumentException("암호화된 소셜 ID는 필수입니다.");
        }
        return new SocialId(encrypted.trim());
    }

    public String decrypt() {
        return AESUtil.decrypt(encrypted);
    }
}
