package com.example.demo.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        String token = extractTokenFromCookie(request);

        if (token != null) {
            try {
                DecodedJWT jwt = jwtTokenProvider.verifyToken(token);
                Long memberId = jwt.getClaim("memberId").asLong();

                request.setAttribute("memberId", memberId);
            } catch (Exception e) {
                // 🚨 잘못된 토큰 → 인증 해제하고 null로 세팅
                System.out.println("Invalid JWT: " + e.getMessage());
                request.setAttribute("memberId", null);
            }
        } else {
            // 🚨 토큰 자체가 없으면 null 세팅
            request.setAttribute("memberId", null);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
