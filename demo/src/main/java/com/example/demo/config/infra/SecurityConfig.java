package com.example.demo.config.infra;

import com.example.demo.config.jwt.JwtCookieFilter;
import com.example.demo.config.jwt.SameSiteCookieFilter;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final SameSiteCookieFilter sameSiteCookieFilter;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 필터 순서: SameSite 처리 -> JWT 인증 -> ID/PW 인증
                .addFilterBefore(sameSiteCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtCookieFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // ✅ 토스 연동 API (무조건 열기)
                        .requestMatchers(
                                "/api/v1/toss/login",
                                "/api/v1/toss/disconnect"
                        ).permitAll()

                        // 기존 공개 API
                        .requestMatchers(
                                "/auth/**",
                                "/phone/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 프로필은 로그인 필요
                        .requestMatchers("/profile/**").authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
