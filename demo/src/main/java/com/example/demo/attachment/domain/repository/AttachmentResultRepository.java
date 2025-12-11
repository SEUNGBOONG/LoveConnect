package com.example.demo.attachment.domain.repository;

import com.example.demo.attachment.domain.entity.AttachmentResult;
import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentResultRepository extends JpaRepository<AttachmentResult, Long> {
    List<AttachmentResult> findAllByMember(Member member);
    List<AttachmentResult> findByMember(Member member);
}
