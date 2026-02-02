package com.example.demo.config.infra;

import com.example.demo.config.jwt.SameSiteCookieFilter;
import com.example.demo.config.jwt.JwtCookieFilter;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

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
                .logout(logout -> logout.disable())
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // 🔥 핵심: JWT 기반 → 세션 완전 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(STATELESS)
                )

                // 🔥 필터 순서
                .addFilterBefore(sameSiteCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new JwtCookieFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증 없이 허용
                        .requestMatchers("/login", "/logout", "/reset-password").permitAll()
                        .requestMatchers("/api/v1/toss/login").permitAll()
                        .requestMatchers("/normalMembers").permitAll()
                        .requestMatchers("/phone/**").permitAll()

                        // Swagger
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
