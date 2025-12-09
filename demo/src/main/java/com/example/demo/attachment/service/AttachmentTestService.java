package com.example.demo.attachment.service;

import com.example.demo.attachment.domain.entity.AttachmentQuestion;
import com.example.demo.attachment.domain.entity.AttachmentResult;
import com.example.demo.attachment.domain.entity.AttachmentType;
import com.example.demo.attachment.domain.repository.AttachmentQuestionRepository;
import com.example.demo.attachment.domain.repository.AttachmentResultRepository;
import com.example.demo.attachment.dto.request.AttachmentAnswerRequest;
import com.example.demo.attachment.dto.request.AttachmentSubmitRequest;
import com.example.demo.attachment.dto.response.AttachmentResultResponse;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import com.example.demo.login.member.domain.member.Member;
import com.example.demo.login.member.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentTestService {

    private final AttachmentQuestionRepository questionRepository;
    private final AttachmentResultRepository resultRepository;
    private final MemberJpaRepository memberRepository;

    public AttachmentResultResponse evaluate(Long memberId, AttachmentSubmitRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        double anxiousSum = 0;
        double avoidantSum = 0;
        int anxiousCount = 0;
        int avoidantCount = 0;

        for (AttachmentAnswerRequest answer : request.answers()) {
            AttachmentQuestion q = questionRepository.findById(answer.questionId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND));

            if (q.getType() == AttachmentType.ANXIOUS) {
                anxiousSum += answer.score();
                anxiousCount++;
            } else {
                avoidantSum += answer.score();
                avoidantCount++;
            }
        }

        double anxiousAvg = anxiousSum / anxiousCount;
        double avoidantAvg = avoidantSum / avoidantCount;

        String resultType = getResultType(anxiousAvg, avoidantAvg);

        // ⭐ 결과 저장
        AttachmentResult result = AttachmentResult.builder()
                .member(member)
                .resultType(resultType)
                .anxiousScore(anxiousAvg)
                .avoidantScore(avoidantAvg)
                .createdAt(String.valueOf(System.currentTimeMillis()))
                .build();

        resultRepository.save(result);

        return new AttachmentResultResponse(resultType, anxiousAvg, avoidantAvg);
    }

    @Transactional(readOnly = true)
    public List<AttachmentQuestion> getQuestions() {
        return questionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AttachmentResultResponse> getResultHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        return resultRepository.findAllByMember(member).stream()
                .map(result -> new AttachmentResultResponse(
                        result.getResultType(),
                        result.getAnxiousScore(),
                        result.getAvoidantScore()
                ))
                .toList();
    }

    private String getResultType(double anxious, double avoidant) {
        if (anxious >= 3 && avoidant >= 3) return "공포회피형";
        if (anxious >= 3) return "불안형";
        if (avoidant >= 3) return "회피형";
        return "안정형";
    }
}
