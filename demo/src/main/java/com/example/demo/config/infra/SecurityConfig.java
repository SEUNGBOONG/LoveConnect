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
                        // ✅ 토스
                        .requestMatchers(
                                "/api/v1/toss/login",
                                "/api/v1/toss/disconnect"
                        ).permitAll()

                        // ✅ Swagger
                        .requestMatchers(
                                "/auth/**",
                                "/phone/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 🔥🔥🔥 이 두 줄이 핵심 🔥🔥🔥
                        .requestMatchers(
                                org.springframework.http.HttpMethod.OPTIONS,
                                "/profile/**"
                        ).permitAll()

                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/profile/member"
                        ).permitAll()

                        // 실제 보호
                        .requestMatchers("/profile/**").authenticated()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
