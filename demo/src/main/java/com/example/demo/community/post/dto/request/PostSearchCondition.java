package com.example.demo.community.post.dto.request;

/**
 * 게시글 검색 조건 DTO
 * @param type 검색 필드 (예: TITLE, CONTENT, WRITER_NICKNAME)
 * @param keyword 검색어
 */
public record PostSearchCondition(String type, String keyword) { }
