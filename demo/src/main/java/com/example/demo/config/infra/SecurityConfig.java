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

                .addFilterBefore(sameSiteCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtCookieFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // ✅ 토스 연동 API
                        .requestMatchers(
                                "/api/v1/toss/login",
                                "/api/v1/toss/disconnect"
                        ).permitAll()

                        // ✅ Swagger / 공개 API
                        .requestMatchers(
                                "/auth/**",
                                "/phone/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 🔥🔥🔥 여기 추가 🔥🔥🔥
                        // Swagger에서 회원탈퇴 테스트 허용
                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/profile/member"
                        ).permitAll()

                        // 실제 서비스용 (그 외 프로필 API)
                        .requestMatchers("/profile/**").authenticated()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
