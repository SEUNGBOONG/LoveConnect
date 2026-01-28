package com.example.demo.config.jwt;

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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // ✅ permitAll 경로는 필터에서 완전히 스킵
        if (isPermitAllPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromCookie(request);

        // 헤더에서도 토큰을 찾을 수 있도록 보완 (선택사항이나 권장)
        if (token == null) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                token = bearerToken.substring(7);
            }
        }

        if (token != null && !token.isBlank()) {
            try {
                DecodedJWT jwt = jwtTokenProvider.verifyToken(token);
                Long memberId = jwt.getClaim("memberId").asLong();

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(memberId, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                // 인증이 필요한 경로인데 토큰이 잘못된 경우 에러를 던지거나 그대로 진행
                // 여기서는 시큐리티가 처리하도록 filterChain으로 넘깁니다.
            }
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
        return uri.equals("/login")
                || uri.equals("/api/v1/toss/login") // ✅ 토스 로그인 경로 추가
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
