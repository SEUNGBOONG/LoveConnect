package com.example.demo.login.member.infrastructure.member;

import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long> {

    boolean existsByMemberNickName(String memberNickName);
    boolean existsByMemberEmail(String memberEmail);
    Optional<Member> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber); // ✅ 추가

    Optional<Member> findMemberByMemberEmail(String memberEmail);
    List<Member> findAll();
}
