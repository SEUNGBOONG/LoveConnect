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

        // 🔥 인증 자체가 안 된 경우
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(CustomErrorCode.NOT_FIND_TOKEN);
        }

        // 🔥 JwtCookieFilter에서 넣어준 memberId 사용
        try {
            return Long.parseLong(authentication.getPrincipal().toString());
        } catch (NumberFormatException e) {
            throw new CustomException(CustomErrorCode.EXPIRED_TOKEN);
        }
    }
}
