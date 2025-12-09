package com.example.demo.community.comment.domain.repository;

import com.example.demo.community.comment.domain.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Comment> findAllByPostWithWriter(Long postId) {
        return em.createQuery("""
                select c from Comment c
                join fetch c.writer
                left join fetch c.parent
                where c.post.id = :postId
                order by c.id asc
                """, Comment.class)
                .setParameter("postId", postId)
                .getResultList();
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
