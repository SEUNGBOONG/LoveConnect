package com.example.demo.community.post.domain.entity;

import com.example.demo.login.member.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // ✅ JPA Auditing 활성화
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ 전략 명시 권장
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id") // ✅ 외래키 컬럼명 명시 권장
    private Member writer;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @CreatedDate // ✅ 생성 시점 자동 주입
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ✅ 생성자: 필수 값만 받음
    public Post(Member writer, String title, String content) {
        this.writer = writer;
        this.title = title;
        this.content = content;
    }

    // ✅ 수정 메서드
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
