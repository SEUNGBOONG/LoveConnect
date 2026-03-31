package com.example.demo.login.global.resolver;

import com.example.demo.login.global.annotation.Member;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Component
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Member.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        Member memberAnnotation = parameter.getParameterAnnotation(Member.class);
        boolean required = memberAnnotation == null || memberAnnotation.required();

        // 🔥 인증 자체가 안 된 경우
        if (authentication == null || authentication.getPrincipal() == null) {
            if (!required) {
                return null;
            }
            throwTokenExceptionByContext(webRequest);
        }

        // 🔥 익명 사용자(토큰 없음)인 경우
        if ("anonymousUser".equals(authentication.getPrincipal().toString())) {
            if (!required) {
                return null;
            }
            throwTokenExceptionByContext(webRequest);
        }

        // 🔥 JwtCookieFilter에서 넣어준 memberId 사용
        try {
            return Long.parseLong(authentication.getPrincipal().toString());
        } catch (NumberFormatException e) {
            // 숫자 principal이 아니면(비정상 인증), 토큰이 없거나 인증 실패로 본다.
            if (!required) {
                return null;
            }
            throwTokenExceptionByContext(webRequest);
            throw new IllegalStateException("Unreachable");
        }
    }

    private void throwTokenExceptionByContext(NativeWebRequest webRequest) {
        Object expired = webRequest.getAttribute("tokenExpired", SCOPE_REQUEST);
        if (Boolean.TRUE.equals(expired)) {
            throw new CustomException(CustomErrorCode.EXPIRED_TOKEN);
        }
        throw new CustomException(CustomErrorCode.NOT_FIND_TOKEN);
    }
}
