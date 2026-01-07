package com.example.demo.community.comment.domain.repository;

import com.example.demo.community.comment.domain.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Comment> findParentCommentsWithWritersAndChildren(Long postId, Pageable pageable) {

        List<Comment> parentComments = em.createQuery("""
            select c from Comment c
            join fetch c.writer
            where c.post.id = :postId and c.parent is null
            order by c.createdAt desc
            """, Comment.class)
                .setParameter("postId", postId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<Long> parentIds = parentComments.stream()
                .map(Comment::getId)
                .toList();

        if (parentIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Comment> childrenComments = em.createQuery("""
            select c from Comment c
            join fetch c.writer
            where c.parent.id in :parentIds
            order by c.parent.id asc, c.createdAt asc
            """, Comment.class)
                .setParameter("parentIds", parentIds)
                .getResultList();

        parentComments.forEach(parent -> {
            parent.getChildren().clear();
            parent.getChildren().addAll(
                    childrenComments.stream()
                            .filter(child -> child.getParent().getId().equals(parent.getId()))
                            .toList()
            );
        });

        Long total = em.createQuery("""
            select count(c) from Comment c
            where c.post.id = :postId and c.parent is null
            """, Long.class)
                .setParameter("postId", postId)
                .getSingleResult();

        return new PageImpl<>(parentComments, pageable, total);
    }

    @Override
    public Optional<Comment> findByIdWithWriter(Long commentId) {
        return em.createQuery("""
                select c from Comment c
                join fetch c.writer
                where c.id = :commentId
                """, Comment.class)
                .setParameter("commentId", commentId)
                .getResultList()
                .stream()
                .findFirst();
    }
}
