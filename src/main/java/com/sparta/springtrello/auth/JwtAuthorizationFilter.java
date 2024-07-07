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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserAdapter userAdapter;
    private final ObjectMapper objectMapper;

    private final Map<String, List<String>> whiteList = Map.of(
            "/", List.of(HttpMethod.GET.name()),
            "/error", List.of(HttpMethod.GET.name()),
            "/users/signup", List.of(HttpMethod.GET.name(), HttpMethod.POST.name()),
            "/users/login", List.of(HttpMethod.GET.name(), HttpMethod.POST.name())
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
        String method = req.getMethod();
        log.info("요청된 URI: {}", uri);

        // 화이트 리스트 확인
        if (isWhiteListed(uri, method)) {
            log.info("인증이 필요 없는 요청: {} {}", method, uri);
            filterChain.doFilter(req, res);
            return;
        }

        // HTTP 요청 헤더에서 JWT 토큰 값을 가져옴. 요청헤더에서 토큰 추출
        String accessToken = jwtProvider.getAccessTokenFromHeader(req);
        try {
            if (!StringUtils.hasText(accessToken)) {
                setErrorResponse(res, ResponseCodeEnum.INVALID_TOKENS);
                return;
            }

            // 액세스 토큰에서 클레임(사용자 정보)을 추출
            Claims accessTokenClaims = jwtProvider.getUserInfoFromToken(accessToken);
            String username = accessTokenClaims.getSubject();
            User user = userAdapter.findByUsername(username);

            // 유저의 리프레쉬 토큰이 null인 경우
            if (user == null || user.getRefreshToken() == null) {
                setErrorResponse(res, ResponseCodeEnum.INVALID_TOKENS);
                return;
            }

            boolean accessTokenValid = jwtProvider.validateToken(accessToken);
            if (accessTokenValid) {
                log.info("유효한 액세스 토큰 처리");
                handleValidAccessToken(accessToken); // 엑세스 토큰의 유효성을 검증합니다.
            } else {
                setErrorResponse(res, ResponseCodeEnum.INVALID_TOKENS);
                return;
            }
        } catch (ExpiredJwtException e) {
            // 액세스 토큰이 만료된 경우 리프레시 토큰을 통해 액세스 토큰을 재발급 시도
            log.info("만료된 액세스 토큰 처리");
            handleExpiredAccessToken(req, res);
            return;
        } catch (JwtException | IllegalArgumentException e) {
            // 그 외의 잘못된 토큰인 경우
            setErrorResponse(res, ResponseCodeEnum.INVALID_TOKENS);
            return;
        }
        filterChain.doFilter(req, res);
    }

    // 유효한 Access Token 처리
    private void handleValidAccessToken(String accessToken) {
        // 액세스 토큰에서 클레임(사용자 정보)을 추출
        Claims accessTokenClaims = jwtProvider.getUserInfoFromToken(accessToken);
        String username = accessTokenClaims.getSubject();

        // 사용자 인증 설정
        setAuthentication(username);
    }

    // 액세스 토큰이 만료된 경우 리프레시 토큰을 통해 액세스 토큰을 재발급
    private void handleExpiredAccessToken(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String refreshToken = jwtProvider.getRefreshTokenFromHeader(req);
        // refreshToken이 null이 아니고 비어 있지 않으며 유효한 텍스트를 포함하고 있는지 확인, 유효성 확인
        if (StringUtils.hasText(refreshToken) && jwtProvider.validateToken(refreshToken)) {
            Claims refreshTokenClaims = jwtProvider.getUserInfoFromToken(refreshToken);
            String username = refreshTokenClaims.getSubject();

            // 데이터베이스에서 리프레시 토큰 확인
            User user = userAdapter.findByUsername(username);
            if (user != null && refreshToken.equals(user.getRefreshToken())) {
                String newAccessToken = jwtProvider.createAccessToken(username);
                res.addHeader(JwtProvider.AUTHORIZATION_HEADER, newAccessToken);
                setAuthentication(username);
            } else {
                setErrorResponse(res, ResponseCodeEnum.INVALID_TOKENS);
            }
        } else {
            setErrorResponse(res, ResponseCodeEnum.REFRESH_TOKEN_EXPIRED);
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
        return new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
    }

    // 예외를 처리하고 HTTP 응답을 설정하는 메서드
    private void setErrorResponse(HttpServletResponse res, ResponseCodeEnum responseCodeEnum) throws IOException {
        ResponseEntity<HttpResponseDto<Void>> responseEntity = ResponseUtils.error(responseCodeEnum);
        res.setStatus(responseEntity.getStatusCode().value());
        res.setContentType("application/json;charset=UTF-8");
        ServletOutputStream outputStream = res.getOutputStream();
        objectMapper.writeValue(outputStream, responseEntity.getBody());
        outputStream.flush();
    }

    // 화이트 리스트 검사 메서드
    private boolean isWhiteListed(String uri, String method) {
        return whiteList.getOrDefault(uri, List.of()).contains(method);
    }
}
