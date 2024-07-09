package com.sparta.springtrello.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.springtrello.common.HttpResponseDto;
import com.sparta.springtrello.common.ResponseCodeEnum;
import com.sparta.springtrello.common.ResponseUtils;
import com.sparta.springtrello.domain.user.entity.User;
import com.sparta.springtrello.domain.user.repository.UserAdapter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserAdapter userAdapter;
    private final ObjectMapper objectMapper;

    private final List<String> anyMethodWhiteList = List.of(
            "/", "/error", "/users/signup", "/users/login", "/users/kakao/authorize", "/users/kakao/callback"
    );

    public JwtAuthorizationFilter(JwtProvider jwtProvider, UserDetailsServiceImpl userDetailsService,
                                  UserAdapter userAdapter, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.userAdapter = userAdapter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String uri = req.getRequestURI();
        log.info("요청된 URI: {}", uri);

        // 화이트 리스트 확인
        if (isWhiteListed(uri)) {
            log.info("인증이 필요 없는 요청: {}", uri);
            filterChain.doFilter(req, res);
            return;
        }

        try {
            String accessToken = jwtProvider.getAccessTokenFromHeader(req);
            if (!StringUtils.hasText(accessToken)) {
                setErrorResponse(res);
                return;
            }

            Claims accessTokenClaims = jwtProvider.getUserInfoFromToken(accessToken);
            String username = accessTokenClaims.getSubject();
            User user = userAdapter.findByUsername(username);

            if (user == null || user.getRefreshToken() == null) {
                setErrorResponse(res);
                return;
            }

            boolean accessTokenValid = jwtProvider.validateToken(accessToken);
            if (accessTokenValid) {
                log.info("유효한 액세스 토큰 처리");
                handleValidAccessToken(accessToken);
            } else {
                setErrorResponse(res);
                return;
            }
        } catch (ExpiredJwtException e) {
            log.info("만료된 액세스 토큰 처리");
            handleExpiredAccessToken(req, res);
            return;
        } catch (JwtException | IllegalArgumentException | AuthenticationServiceException e) {
            setErrorResponse(res);
            return;
        }

        filterChain.doFilter(req, res);
    }

    // 유효한 Access Token 처리
    private void handleValidAccessToken(String accessToken) {
        Claims accessTokenClaims = jwtProvider.getUserInfoFromToken(accessToken);
        String username = accessTokenClaims.getSubject();
        setAuthentication(username);
    }

    // 액세스 토큰이 만료된 경우 리프레시 토큰을 통해 액세스 토큰을 재발급
    private void handleExpiredAccessToken(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = jwtProvider.getRefreshTokenFromHeader(req);
        if (StringUtils.hasText(refreshToken) && jwtProvider.validateToken(refreshToken)) {
            Claims refreshTokenClaims = jwtProvider.getUserInfoFromToken(refreshToken);
            String username = refreshTokenClaims.getSubject();

            User user = userAdapter.findByUsername(username);
            if (user != null && refreshToken.equals(user.getRefreshToken())) {
                String newAccessToken = jwtProvider.createAccessToken(username);
                res.addHeader(JwtProvider.AUTHORIZATION_HEADER, newAccessToken);
                setAuthentication(username);
            } else {
                setErrorResponse(res);
            }
        } else {
            setErrorResponse(res);
        }
    }

    // 인증 객체를 생성하여 SecurityContext에 설정하기 위한 메서드
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성 매서드
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private void writeResponseBody(HttpServletResponse res, ResponseEntity<HttpResponseDto<Void>> responseEntity) throws IOException {
        res.setStatus(responseEntity.getStatusCode().value());
        res.setContentType("application/json;charset=UTF-8");
        try (ServletOutputStream outputStream = res.getOutputStream()) {
            objectMapper.writeValue(outputStream, responseEntity.getBody());
            outputStream.flush();
        } catch (IOException e) {
            log.error("응답 본문 쓰기 실패: {}", e.getMessage());
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");
            res.getWriter().write("{\"statusCode\": 500, \"message\": \"Internal Server Error\"}");
        }
    }

    private void setErrorResponse(HttpServletResponse response) {
        ResponseEntity<HttpResponseDto<Void>> responseEntity = ResponseUtils.error(ResponseCodeEnum.INVALID_TOKENS);
        try {
            writeResponseBody(response, responseEntity);
        } catch (IOException e) {
            log.error("에러 응답 본문 쓰기 실패: {}", e.getMessage());
        }
    }

    // 화이트 리스트 검사 메서드
    private boolean isWhiteListed(String uri) {
        return anyMethodWhiteList.stream().anyMatch(uri::equals);
    }
}
