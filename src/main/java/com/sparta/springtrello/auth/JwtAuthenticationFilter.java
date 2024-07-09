package com.sparta.springtrello.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.springtrello.common.HttpResponseDto;
import com.sparta.springtrello.common.ResponseCodeEnum;
import com.sparta.springtrello.common.ResponseUtils;
import com.sparta.springtrello.domain.user.dto.LoginRequestDto;
import com.sparta.springtrello.domain.user.entity.User;
import com.sparta.springtrello.domain.user.entity.UserStatusEnum;
import com.sparta.springtrello.domain.user.repository.UserAdapter;
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

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("JwtAuthenticationFilter: 인증 시도 시작");
        try {
            LoginRequestDto requestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            User user;
            try {
                user = userAdapter.findByUsername(requestDto.getUsername());
            } catch (Exception e) {
                setErrorResponse(response, ResponseCodeEnum.USER_NOT_FOUND);
                return null;
            }

            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                setErrorResponse(response, ResponseCodeEnum.PASSWORD_INCORRECT);
                return null;
            }

            if (user.getUserStatus() == UserStatusEnum.STATUS_DELETED) {
                setErrorResponse(response, ResponseCodeEnum.USER_DELETED);
                return null;
            }

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            setErrorResponse(response, ResponseCodeEnum.INVALID_TOKENS);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        log.info("JwtAuthenticationFilter: 인증 성공");
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();

        String accessToken = jwtProvider.createAccessToken(username);
        String refreshToken = jwtProvider.createRefreshToken(username);

        response.addHeader(JwtProvider.AUTHORIZATION_HEADER, accessToken);

        User user = ((UserDetailsImpl) authResult.getPrincipal()).getUser();
        user.setRefreshToken(refreshToken);
        userAdapter.save(user);

        response.setStatus(HttpServletResponse.SC_OK);

        ResponseEntity<HttpResponseDto<Void>> responseEntity = ResponseUtils.success(HttpStatus.OK);
        writeResponseBody(response, responseEntity);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.info("JwtAuthenticationFilter: 인증 실패");
        setErrorResponse(response, ResponseCodeEnum.LOGIN_FAILED);
    }

    private void writeResponseBody(HttpServletResponse response, ResponseEntity<HttpResponseDto<Void>> responseEntity) throws IOException {
        response.setStatus(responseEntity.getStatusCode().value());
        response.setContentType("application/json");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            objectMapper.writeValue(outputStream, responseEntity.getBody());
            outputStream.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"statusCode\": 500, \"message\": \"Internal Server Error\"}");
        }
    }

    private void setErrorResponse(HttpServletResponse response, ResponseCodeEnum responseCode) {
        ResponseEntity<HttpResponseDto<Void>> responseEntity = ResponseUtils.error(responseCode);
        try {
            writeResponseBody(response, responseEntity);
        } catch (IOException e) {
            log.error("에러 응답 본문 쓰기 실패: {}", e.getMessage());
        }
    }
}
