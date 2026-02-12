package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;
import com.example.demo.community.post.dto.request.PostSearchCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Page<Post> findAllWithWriter(Pageable pageable) {
        String jpql = "select p from Post p join fetch p.writer order by p.createdAt desc";
        String countJpql = "select count(p) from Post p";

        List<Post> content = em.createQuery(jpql, Post.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long count = em.createQuery(countJpql, Long.class)
                .getSingleResult();

        return new PageImpl<>(content, pageable, count);
    }
    // ✅ findAllWithWriters() 제거

    /** ✅ 게시글 검색 쿼리 구현 */
    @Override
    public Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable) {
        // JPQL을 사용하여 동적 검색 구현 (Querydsl을 사용하는 것이 더 효율적입니다.)
        String jpql = "select p from Post p join fetch p.writer w where 1=1";
        String countJpql = "select count(p) from Post p where 1=1";

        // 검색 조건 추가
        if (condition.type() != null && condition.keyword() != null && !condition.keyword().isBlank()) {
            switch (condition.type()) {
                case "TITLE" -> {
                    jpql += " and p.title like :keyword";
                    countJpql += " and p.title like :keyword";
                }
                case "CONTENT" -> {
                    jpql += " and p.content like :keyword";
                    countJpql += " and p.content like :keyword";
                }
                case "WRITER_NICKNAME" -> {
                    jpql += " and w.memberNickName like :keyword";
                    countJpql += " and p.writer.memberNickName like :keyword"; // count는 fetch join 불필요
                }
                // 다른 조건 추가 가능
            }
        }

        jpql += " order by p.createdAt desc";

        // 데이터 조회 쿼리
        var query = em.createQuery(jpql, Post.class);
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            query.setParameter("keyword", "%" + condition.keyword() + "%");
        }

        List<Post> content = query
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // 카운트 쿼리
        var countQuery = em.createQuery(countJpql, Long.class);
        if (condition.keyword() != null && !condition.keyword().isBlank()) {
            countQuery.setParameter("keyword", "%" + condition.keyword() + "%");
        }

        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
