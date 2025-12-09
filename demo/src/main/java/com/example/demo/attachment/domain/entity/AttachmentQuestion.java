package com.example.demo.attachment.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentQuestion {
    @Id
    @GeneratedValue
    private Long id;

    private String content;

    @Enumerated(EnumType.STRING)
    private AttachmentType type; // 불안형/회피형 분류용
}
