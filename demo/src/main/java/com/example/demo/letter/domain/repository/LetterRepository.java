package com.example.demo.letter.domain.repository;

import com.example.demo.letter.domain.Letter;
import com.example.demo.login.member.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long> {
    Page<Letter> findAllByOwnerAndDeletedFalse(Member owner, Pageable pageable);

    Optional<Letter> findByIdAndDeletedFalse(Long id);
}
