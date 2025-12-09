package com.example.demo.community.comment.service;

import com.example.demo.community.comment.domain.entity.Comment;
import com.example.demo.community.comment.domain.repository.CommentRepository;
import com.example.demo.community.comment.dto.request.CommentCreateRequest;
import com.example.demo.community.comment.dto.request.CommentUpdateRequest;
import com.example.demo.community.comment.dto.response.CommentResponse;
import com.example.demo.community.post.domain.repository.PostRepository;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.community.post.domain.entity.Post;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberJpaRepository memberRepository;

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
    }

    private Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.POST_NOT_FOUND));
    }

    /** ✅ 댓글 작성 (대댓글 포함) */
    @Transactional
    public CommentResponse create(Long memberId, CommentCreateRequest request) {
        Post post = getPost(request.postId());
        Member writer = getMember(memberId);

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));
        }

        Comment comment = new Comment(post, writer, request.content(), parent);
        Comment saved = commentRepository.save(comment);

        return CommentResponse.basic(saved);
    }

    /** ✅ 댓글 수정 */
    @Transactional
    public CommentResponse update(Long memberId, Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdWithWriter(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        Member writer = getMember(memberId);
        if (!comment.isWriter(writer)) {
            throw new CustomException(CustomErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.updateContent(request.content());
        return CommentResponse.basic(comment);
    }

    /** ✅ 댓글 삭제 (대댓글도 함께 삭제됨) */
    @Transactional
    public void delete(Long memberId, Long commentId) {
        Comment comment = commentRepository.findByIdWithWriter(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        Member writer = getMember(memberId);
        if (!comment.isWriter(writer)) {
            throw new CustomException(CustomErrorCode.COMMENT_UNAUTHORIZED);
        }

        commentRepository.delete(comment); // Cascade 옵션으로 children 자동 삭제
    }

    /** ✅ 게시글별 댓글 전체조회 (children 포함) */
    @Transactional(readOnly = true)
    public List<CommentResponse> getByPost(Long postId) {
        return commentRepository.findAllByPostWithWriter(postId).stream()
                .filter(Comment::isParent)
                .map(CommentResponse::from)
                .toList();
    }

    /** ✅ 게시글 댓글 페이징 (부모 기준) */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getByPostPaged(Long postId, Pageable pageable) {
        return commentRepository.findAllByPostIdAndParentIsNull(postId, pageable)
                .map(CommentResponse::from);
    }
}
