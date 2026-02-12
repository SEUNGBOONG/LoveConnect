package com.example.demo.community.post.domain.repository;

import com.example.demo.community.post.domain.entity.Post;
import com.example.demo.community.post.dto.request.PostSearchCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
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
        // 데이터 조회 (N+1 방지 fetch join)
        String jpql = "select p from Post p join fetch p.writer order by p.createdAt desc";
        // 카운트 조회 (fetch join 절대 금지)
        String countJpql = "select count(p) from Post p";

        List<Post> content = em.createQuery(jpql, Post.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = em.createQuery(countJpql, Long.class).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" where 1=1");
        boolean hasKeyword = condition.type() != null && condition.keyword() != null && !condition.keyword().isBlank();

        // 닉네임 검색이 아닐 경우에도 writer 조인은 필요 (데이터 일관성)
        String baseJpql = "select p from Post p join fetch p.writer w";
        String countBaseJpql = "select count(p) from Post p join p.writer w";

        if (hasKeyword) {
            switch (condition.type()) {
                case "TITLE" -> whereClause.append(" and p.title like :keyword");
                case "CONTENT" -> whereClause.append(" and p.content like :keyword");
                case "WRITER_NICKNAME" -> whereClause.append(" and w.memberNickName like :keyword");
            }
        }

        // 1. 데이터 조회 쿼리
        TypedQuery<Post> query = em.createQuery(baseJpql + whereClause + " order by p.createdAt desc", Post.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        // 2. 카운트 조회 쿼리
        TypedQuery<Long> countQuery = em.createQuery(countBaseJpql + whereClause, Long.class);

        if (hasKeyword) {
            query.setParameter("keyword", "%" + condition.keyword() + "%");
            countQuery.setParameter("keyword", "%" + condition.keyword() + "%");
        }

        List<Post> content = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
