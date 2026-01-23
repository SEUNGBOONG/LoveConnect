package com.example.demo.login.global.resolver;

import com.example.demo.login.global.annotation.LoginMember;
import com.example.demo.login.global.application.JwtTokenService;
import com.example.demo.login.global.exception.exceptions.CustomErrorCode;
import com.example.demo.login.global.exception.exceptions.CustomException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
@Component
@RequiredArgsConstructor
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_HEADER_NAME = "Authorization";

    private final JwtTokenService jwtTokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String token = null;

        // 1) Header에서 토큰 읽기
        String tokenHeader = request.getHeader(TOKEN_HEADER_NAME);
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
        }

        // 2) Cookie에서 토큰 읽기
        if (token == null && request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> "token".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // 3) 토큰 없으면 바로 401
        if (token == null || token.isBlank()) {
            throw new CustomException(CustomErrorCode.NOT_FIND_TOKEN);
        }

        try {
            // 정상적으로 memberId 반환
            return jwtTokenService.verifyAndExtractJwtToken(token);
        } catch (Exception e) {
            // 4) 토큰 만료 / 변조 → 401
            throw new CustomException(CustomErrorCode.EXPIRED_TOKEN);
        }
    }
}
