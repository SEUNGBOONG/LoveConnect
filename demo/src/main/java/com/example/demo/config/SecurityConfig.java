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
    private final SameSiteCookieFilter sameSiteCookieFilter;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable())
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // 🔥 필터 순서 보장
                .addFilterBefore(sameSiteCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new JwtCookieFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증 없이 허용
                        .requestMatchers("/login", "/logout", "/reset-password").permitAll()
                        .requestMatchers("/normalMembers").permitAll()
                        .requestMatchers("/phone/**").permitAll()

                        // Swagger
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // 게시글 조회는 공개
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
