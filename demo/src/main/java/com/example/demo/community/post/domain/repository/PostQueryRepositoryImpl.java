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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
public class PostQueryRepositoryImpl implements PostQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Post> findWithWriterById(Long postId) {
        // 단건 조회는 성능상 fetch join이 유리함
        return em.createQuery("""
                        select p from Post p
                        join fetch p.writer
                        where p.id = :postId
                        """, Post.class)
                .setParameter("postId", postId)
                .getResultList() // getSingleResult는 결과 없을 때 예외 발생하므로 리스트 처리
                .stream()
                .findFirst();
    }

    @Override
    public Page<Post> findAllWithWriter(Pageable pageable) {
        // 전체 조회 (조건 없음)
        return executeSearch(null, pageable);
    }

    @Override
    public Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable) {
        // 조건 검색
        return executeSearch(condition, pageable);
    }

    /**
     * ✅ 검색 로직 공통화 및 분리
     * 중복 코드를 제거하고 카운트 쿼리와 데이터 쿼리의 일관성을 유지합니다.
     */
    private Page<Post> executeSearch(PostSearchCondition condition, Pageable pageable) {
        // 1. 기본 쿼리 골격 (fetch join 유무 차이)
        String baseJpql = "select p from Post p join fetch p.writer w";
        String countBaseJpql = "select count(p) from Post p join p.writer w"; // fetch join 제거

        // 2. 동적 Where 절 생성
        StringBuilder whereClause = new StringBuilder(" where 1=1");
        boolean hasKeyword = hasText(condition);

        if (hasKeyword) {
            switch (condition.type()) {
                case "TITLE" -> whereClause.append(" and p.title like :keyword");
                case "CONTENT" -> whereClause.append(" and p.content like :keyword");
                case "WRITER_NICKNAME" -> whereClause.append(" and w.memberNickName like :keyword");
                // 잘못된 타입이 들어오면 검색 조건 무시 (혹은 예외 처리 가능)
            }
        }

        // 3. 데이터 조회 쿼리 생성
        TypedQuery<Post> query = em.createQuery(baseJpql + whereClause + " order by p.createdAt desc", Post.class)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        // 4. 카운트 조회 쿼리 생성
        TypedQuery<Long> countQuery = em.createQuery(countBaseJpql + whereClause, Long.class);

        // 5. 파라미터 바인딩 (두 쿼리에 동일하게 적용)
        if (hasKeyword) {
            String likeValue = "%" + condition.keyword() + "%";
            query.setParameter("keyword", likeValue);
            countQuery.setParameter("keyword", likeValue);
        }

        List<Post> content = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    // null 또는 공백 체크 헬퍼
    private boolean hasText(PostSearchCondition condition) {
        return condition != null
                && StringUtils.hasText(condition.type())
                && StringUtils.hasText(condition.keyword());
    }
}
