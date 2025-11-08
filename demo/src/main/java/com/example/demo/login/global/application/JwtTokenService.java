package com.example.demo.login.global.application;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.login.global.exception.exceptions.jwt.TokenTimeException;
import com.example.demo.login.member.exception.exceptions.auth.NotFoundTokenException;
import com.example.demo.login.member.infrastructure.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    public static final String MEMBER_ID = "memberId";
    private final JwtTokenProvider jwtTokenProvider;

    public DecodedJWT verifyJwtToken(String token) {
        return jwtTokenProvider.verifyToken(token);
    }

    public Long verifyAndExtractJwtToken(String token) {
        try {
            return validateTokenExist(token);
        } catch (TokenExpiredException e) {
            throw new TokenTimeException();
        }
    }

    private Long validateTokenExist(String token) {
        return Optional.of(extractJwtToken(token))
                .orElseThrow(NotFoundTokenException::new);
    }

    private Long extractJwtToken(String token) {
        return verifyJwtToken(token).getClaim(MEMBER_ID)
                .asLong();
    }
}
