package com.example.demo.config;

import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable())
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        // ✅ OPTIONS 요청 허용 (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ 인증 없이 접근 가능한 API
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/normalMembers").permitAll()
                        .requestMatchers("/phone/**").permitAll()
                        .requestMatchers("/auth/me").authenticated()
                        // ✅ Swagger
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // 🔥 게시글 조회는 로그인 없이 허용
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()

                        // 🔥 게시글 작성/수정/삭제는 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()

                        // ✅ 그 외 인증 필요한 API
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/matches/**").authenticated()
                        .requestMatchers("/comments/**").authenticated()

                        // ✅ 나머지는 모두 인증 필요
                        .anyRequest().authenticated()
                )

                /* ===============================
                   🔥 핵심: 401 / 403 명확히 분리
                   =============================== */
                .exceptionHandling(exception -> exception
                        // 🔐 인증 실패 (쿠키/토큰 없음, 토큰 만료)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                {
                                  "success": false,
                                  "code": "T001",
                                  "message": "인증 정보가 없습니다."
                                }
                                """
                            );
                        })

                        // 🚫 인가 실패 (권한 없음)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                {
                                  "success": false,
                                  "code": "AUTH_403",
                                  "message": "접근 권한이 없습니다."
                                }
                                """
                            );
                        })
                )

                // ✅ JWT 쿠키 필터
                .addFilterBefore(
                        new JwtCookieFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
