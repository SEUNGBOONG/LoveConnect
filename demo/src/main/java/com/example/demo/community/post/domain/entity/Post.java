package com.example.demo.community.post.domain.entity;

import com.example.demo.login.member.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    private String title;
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt; // ✅ 추가

    public Post(Member writer, String title, String content) {
        this.writer = writer;
        this.title = title;
        this.content = content;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public boolean isWriter(Member member) {
        return this.writer.equals(member);
    }
}
