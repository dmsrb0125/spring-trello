package com.sparta.springtrello.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.springtrello.common.HttpResponseDto;
import com.sparta.springtrello.common.ResponseCodeEnum;
import com.sparta.springtrello.common.ResponseUtils;
import com.sparta.springtrello.domain.user.dto.LoginRequestDto;
import com.sparta.springtrello.domain.user.entity.User;
import com.sparta.springtrello.domain.user.entity.UserStatusEnum;
import com.sparta.springtrello.domain.user.repository.UserAdapter;
import com.sparta.springtrello.exception.custom.auth.UserInfoException;
import com.sparta.springtrello.exception.custom.auth.InvalidTokenException;
import com.sparta.springtrello.exception.custom.auth.UserDeletedException;
import com.sparta.springtrello.exception.custom.auth.UserAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j(topic = "로그인 처리 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtProvider jwtProvider;
    private final UserAdapter userAdapter;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, UserAdapter userAdapter, ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
        this.jwtProvider = jwtProvider;
        this.userAdapter = userAdapter;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        setFilterProcessesUrl("/users/login");
    }

    // 로그인 정보 추출 및 인증 시도
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("JwtAuthenticationFilter 시작");
        try {
            LoginRequestDto requestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            User user;
            try {
                user = userAdapter.findByUsername(requestDto.getUsername());
            } catch (Exception e) {
                throw new UserInfoException();
            }

            // 비밀번호가 일치하지 않는 경우
            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                throw new UserInfoException();
            }

            // 탈퇴한 사용자인지 확인
            if (user.getUserStatus() == UserStatusEnum.STATUS_DELETED) {
                throw new UserDeletedException();
            }

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InvalidTokenException();
        }
    }

    // 인증 성공 처리
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        log.info("JwtAuthenticationFilter: 인증 성공");
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();

        String accessToken = jwtProvider.createAccessToken(username);
        String refreshToken = jwtProvider.createRefreshToken(username);

        // 응답 헤더에 Access Token만 담기
        response.addHeader(JwtProvider.AUTHORIZATION_HEADER, accessToken);

        User user = ((UserDetailsImpl) authResult.getPrincipal()).getUser();
        user.setRefreshToken(refreshToken);
        userAdapter.save(user);  // Refresh Token은 유저 필드에 저장

        response.setStatus(HttpServletResponse.SC_OK);

        // 로그인 성공 응답 본문 설정
        ResponseEntity<HttpResponseDto<Void>> responseEntity = ResponseUtils.success(HttpStatus.OK);
        writeResponseBody(response, responseEntity);
    }

    // 인증 실패 처리
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.info("JwtAuthenticationFilter: 인증 실패");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 로그인 실패 응답 본문 설정
        if (failed instanceof UserAuthenticationException) {
            ResponseCodeEnum responseCode = ((UserAuthenticationException) failed).getResponseCode();
            writeErrorResponse(response, responseCode);
        } else {
            writeErrorResponse(response, ResponseCodeEnum.INVALID_TOKENS);
        }
    }

    // 응답을 헤더뿐 아니라 바디에도 추가로 보내주기 위한 메서드
    private void writeResponseBody(HttpServletResponse response, ResponseEntity<HttpResponseDto<Void>> responseEntity) throws IOException {
        response.setStatus(responseEntity.getStatusCode().value());
        response.setContentType("application/json");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            objectMapper.writeValue(outputStream, responseEntity.getBody());
            outputStream.flush();
        }
    }

    // 에러 응답 설정 메서드
    private void writeErrorResponse(HttpServletResponse response, ResponseCodeEnum responseCodeEnum) throws IOException {
        ResponseEntity<HttpResponseDto<Void>> responseEntity = ResponseUtils.error(responseCodeEnum);
        writeResponseBody(response, responseEntity);
    }
}
