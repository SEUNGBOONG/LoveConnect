package com.example.demo.attachment.domain.repository;

import com.example.demo.attachment.domain.entity.AttachmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentQuestionRepository extends JpaRepository<AttachmentQuestion, Long> {}
