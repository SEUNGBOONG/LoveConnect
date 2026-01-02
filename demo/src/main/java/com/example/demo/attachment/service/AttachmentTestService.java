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

    @Transactional
    public AttachmentResultResponse evaluate(Long memberId, AttachmentSubmitRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        // ✅ 기존 결과 삭제 (항상 최신 결과 1개만 유지)
        resultRepository.deleteByMember(member);

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
        String resultDescription = getResultDescription(resultType);

        // ⭐ 결과 저장
        AttachmentResult result = AttachmentResult.builder()
                .member(member)
                .resultType(resultType)
                .anxiousScore(anxiousAvg)
                .avoidantScore(avoidantAvg)
                .createdAt(String.valueOf(System.currentTimeMillis()))
                .build();

        resultRepository.save(result);

        return new AttachmentResultResponse(resultType, anxiousAvg, avoidantAvg, resultDescription);
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
                        result.getAvoidantScore(),
                        getResultDescription(result.getResultType())
                ))
                .toList();
    }

    // 🔍 정교한 분류 로직 (비율 기반)
    private String getResultType(double anxious, double avoidant) {
        if (anxious >= 4 && avoidant >= 4) return "공포회피형";
        if (anxious >= 4) return "불안형";
        if (avoidant >= 4) return "회피형";

        double total = anxious + avoidant;
        double anxiousRate = (anxious / total) * 100;
        double avoidantRate = (avoidant / total) * 100;

        if (anxiousRate >= 40 && anxiousRate <= 60 && avoidantRate >= 40 && avoidantRate <= 60) {
            return "안정형";
        }

        return "불확실";
    }

    // 🔍 유형별 설명 추가
    private String getResultDescription(String resultType) {
        return switch (resultType) {
            case "안정형" -> "당신은 관계에서 안정감을 잘 느끼며 신뢰를 잘 쌓는 유형입니다.";
            case "불안형" -> "당신은 상대의 반응에 민감하며 불안감을 자주 느낄 수 있습니다.";
            case "회피형" -> "당신은 감정 표현에 거리감을 느끼며 독립성을 중요시합니다.";
            case "공포회피형" -> "관계를 원하지만 동시에 두려움을 느끼는 복합적인 성향입니다.";
            default -> "결과 해석이 어렵습니다.";
        };
    }
}
