package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostQueryRepositoryImpl implements PostQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Post> findWithWriterById(Long postId) {
        return em.createQuery("""
                select p from Post p
                join fetch p.writer
                where p.id = :postId
                """, Post.class)
                .setParameter("postId", postId)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Post> findAllWithWriters() {
        return em.createQuery("""
                select p from Post p
                join fetch p.writer
                order by p.id desc
                """, Post.class)
                .getResultList();
    }
}
