package com.sparta.springtrello.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtProvider {

    // HTTP 요청 헤더의 키를 정의
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer "; //  JWT 토큰의 유형을 지정하는 접두사

    // 비밀키
    @Value("${jwt-secret-key}")
    private String secretKey; //  설정 파일에 저장된 Base64 인코딩된 비밀 키 ->  비밀키 객체를 만들기위한 비밀키 문자열
    private Key key; // JWT 서명 및 검증에 사용되는 비밀 키 init() 에서 secretKey를 사용하여 생성

    //  사용할 서명 알고리즘
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 비밀키 객체 생성 메서드
    @PostConstruct // JwtUtil 클래스가 초기화될 때 자동으로 실행 시킨다
    public void initializeSecretKey()  {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes); //  HMAC SHA 알고리즘에 적합한 Key 객체를 생성
    }


    // 토큰 생성 메서드 -> username, 만료시간, 발급시간을 담고, 비밀키와 서명알고리즘을 통해 토큰 생성
    private String createToken(String username, long expirationTime) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .setIssuedAt(now)
                .signWith(key, signatureAlgorithm);

        return BEARER_PREFIX + builder.compact();
    }


    // ACCESS_TOKEN생성
    public String createAccessToken(String username) {
        long ACCESS_TOKEN_TIME = 30 * 60 * 1000L*60 * 60 * 60; // 액세스 토큰의 유효 기간을 30분으로 설정
        return createToken(username, ACCESS_TOKEN_TIME);
    }

    // REFRESH_TOKEN생성
    public String createRefreshToken(String username) {
        long REFRESH_TOKEN_TIME = 14 * 24 * 60 * 60 * 1000L; // 리프레시 토큰의 유효 기간을 2주로 설정
        return createToken(username, REFRESH_TOKEN_TIME);
    }


    // Access Token 헤더에서 JWT 가져오기
    public String getAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // Refresh Token 헤더에서 JWT 가져오기
    public String getRefreshTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(REFRESH_TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // JWT 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // 토큰 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않는 JWT 서명 또는 잘못된 토큰 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰 입니다.");
        }
        return false;
    }




}