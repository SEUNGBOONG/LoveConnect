package com.example.demo.community.comment.service;

import com.example.demo.community.comment.domain.entity.Comment;
import com.example.demo.community.comment.domain.repository.CommentRepository;
import com.example.demo.community.comment.dto.request.CommentCreateRequest;
import com.example.demo.community.comment.dto.request.CommentUpdateRequest;
import com.example.demo.community.comment.dto.response.CommentPageResponse;
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

    /** ✅ 댓글 작성 */
    @Transactional
    public CommentResponse create(Long memberId, CommentCreateRequest request) {
        Post post = getPost(request.postId());
        Member writer = getMember(memberId);

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));
        }

        Comment saved = commentRepository.save(
                new Comment(post, writer, request.content(), parent)
        );

        return CommentResponse.from(saved, memberId);
    }

    /** ✅ 댓글 수정 */
    @Transactional
    public CommentResponse update(Long memberId, Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdWithWriter(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getWriter().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.updateContent(request.content());
        return CommentResponse.from(comment, memberId);
    }

    /** ✅ 댓글 삭제 */
    @Transactional
    public void delete(Long memberId, Long commentId) {
        Comment comment = commentRepository.findByIdWithWriter(commentId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getWriter().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.COMMENT_UNAUTHORIZED);
        }

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getByPostPaged(
            Long postId,
            Pageable pageable,
            Long memberId
    ) {
        Page<Comment> page =
                commentRepository.findParentCommentsWithWritersAndChildren(postId, pageable);

        return page.map(comment -> CommentResponse.from(comment, memberId));
    }
    
}
