package com.example.demo.community.post.service;

import com.example.demo.community.comment.domain.repository.CommentRepository;
import com.example.demo.community.post.domain.entity.Post;
import com.example.demo.community.post.domain.repository.PostRepository;
import com.example.demo.community.post.dto.request.PostCreateRequest;
import com.example.demo.community.post.dto.request.PostSearchCondition;
import com.example.demo.community.post.dto.request.PostUpdateRequest;
import com.example.demo.community.post.dto.response.PostResponse;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public PostResponse create(Long memberId, PostCreateRequest request) {
        Member writer = getMember(memberId);
        Post post = new Post(writer, request.title(), request.content());
        return toResponse(postRepository.save(post), memberId);
    }

    @Transactional
    public PostResponse update(Long memberId, Long postId, PostUpdateRequest request) {
        Post post = getPostWithWriter(postId);
        validateWriter(post, memberId);
        post.update(request.title(), request.content());
        return toResponse(post, memberId);
    }

    @Transactional
    public void delete(Long memberId, Long postId) {
        Post post = getPostWithWriter(postId);
        validateWriter(post, memberId);

        // 🔥 먼저 댓글 삭제
        commentRepository.deleteAllByPostId(postId);

        postRepository.delete(post);
    }

    /** ✅ 상세조회 (isMine 필요) */
    @Transactional(readOnly = true)
    public PostResponse getById(Long memberId, Long postId) {
        Post post = getPostWithWriter(postId);
        return toResponse(post, memberId);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPaged(Long memberId, Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(post -> toResponse(post, memberId));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> search(PostSearchCondition condition, Long memberId, Pageable pageable) {
        return postRepository.searchPosts(condition, pageable)
                .map(post -> toResponse(post, memberId));
    }

    private Post getPostWithWriter(Long id) {
        return postRepository.findWithWriterById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));
    }

    private void validateWriter(Post post, Long memberId) {
        if (!post.getWriter().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.MATCH_MEMBER_NOT_FOUND);
        }
    }

    private Member getMember(Long id) {
        return memberJpaRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MATCH_MEMBER_NOT_FOUND));
    }

    private PostResponse toResponse(Post post, Long memberId) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getWriter().getMemberNickName(),
                post.getWriter().getId().equals(memberId), // ✅ isMine
                post.getCreatedAt()                        // ✅ createdAt
        );
    }
}
