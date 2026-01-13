package com.example.demo.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtCookieFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 🔥 인증 없이 접근 가능한 경로 → 필터 건너뛰기
        if (isPermitAllPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromCookie(request);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT jwt = jwtTokenProvider.verifyToken(token);
            Long memberId = jwt.getClaim("memberId").asLong();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    memberId, null, List.of()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // ❌ 유효하지 않은 토큰이더라도 인증 실패만 하고 통과
            System.out.println("❌ Invalid JWT: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isPermitAllPath(String uri) {
        // 🔥 인증 없이 접근 가능한 경로들 추가
        return uri.equals("/login")
                || uri.equals("/logout")
                || uri.equals("/reset-password")
                || uri.equals("/normalMembers")
                || uri.startsWith("/phone/")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars");
    }
}
