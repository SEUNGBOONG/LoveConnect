package com.example.demo.config;

import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
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

                        // ✅ 인증 없이 접근 가능한 API (로그인/회원가입/비번찾기 등)
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/normalMembers").permitAll()
                        .requestMatchers("/phone/**").permitAll()

                        // ✅ Swagger 문서용
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // ✅ 인증 필요한 API
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/matches/**").authenticated()
                        .requestMatchers("/comments/**").authenticated()
                        .requestMatchers("/posts/**").authenticated()

                        // ✅ 그 외는 모두 인증 필요
                        .anyRequest().authenticated()
                )

                // ✅ JWT 쿠키 필터 적용
                .addFilterBefore(
                        new JwtCookieFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
