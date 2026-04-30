package com.example.demo.letter.service;

import com.example.demo.letter.domain.Letter;
import com.example.demo.letter.domain.LetterCategory;
import com.example.demo.letter.domain.repository.LetterRepository;
import com.example.demo.letter.dto.request.LetterCreateRequest;
import com.example.demo.letter.dto.request.LetterUpdateRequest;
import com.example.demo.letter.dto.response.LetterResponse;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final MemberJpaRepository memberRepository;

    @Transactional
    public LetterResponse create(Long memberId, LetterCreateRequest request) {
        Member owner = getMember(memberId);
        Letter letter = new Letter(
                owner,
                LetterCategory.from(request.category()),
                request.title().trim(),
                request.content().trim()
        );

        return LetterResponse.from(letterRepository.save(letter));
    }

    @Transactional(readOnly = true)
    public Page<LetterResponse> getMine(Long memberId, Pageable pageable) {
        Member owner = getMember(memberId);

        return letterRepository.findAllByOwnerAndDeletedFalse(owner, pageable)
                .map(LetterResponse::from);
    }

    @Transactional(readOnly = true)
    public LetterResponse get(Long memberId, Long letterId) {
        Letter letter = getLetter(letterId);
        validateOwner(letter, memberId);

        return LetterResponse.from(letter);
    }

    @Transactional
    public LetterResponse update(Long memberId, Long letterId, LetterUpdateRequest request) {
        Letter letter = getLetter(letterId);
        validateOwner(letter, memberId);

        letter.update(
                LetterCategory.from(request.category()),
                request.title().trim(),
                request.content().trim()
        );

        return LetterResponse.from(letter);
    }

    @Transactional
    public void delete(Long memberId, Long letterId) {
        Letter letter = getLetter(letterId);
        validateOwner(letter, memberId);
        letter.delete();
    }

    private Letter getLetter(Long letterId) {
        return letterRepository.findByIdAndDeletedFalse(letterId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.LETTER_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateOwner(Letter letter, Long memberId) {
        if (!letter.getOwner().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.LETTER_UNAUTHORIZED);
        }
    }
}
