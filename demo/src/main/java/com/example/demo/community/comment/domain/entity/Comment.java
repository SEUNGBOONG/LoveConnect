package com.example.demo.community.comment.domain.entity;

import com.example.demo.community.post.domain.entity.Post;
import com.example.demo.login.member.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public Comment(Post post, Member writer, String content, Comment parent) {
        this.post = post;
        this.writer = writer;
        this.content = content;
        this.parent = parent;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean isWriter(Member member) {
        return this.writer.equals(member);
    }
}
