package com.sparta.springtrello.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.springtrello.auth.JwtProvider;
import com.sparta.springtrello.auth.UserDetailsImpl;
import com.sparta.springtrello.common.HttpResponseDto;
import com.sparta.springtrello.common.ResponseUtils;
import com.sparta.springtrello.domain.user.dto.ProfileResponseDto;
import com.sparta.springtrello.domain.user.dto.SignupRequestDto;
import com.sparta.springtrello.domain.user.dto.UpdatePasswordRequestDto;
import com.sparta.springtrello.domain.user.dto.UpdateProfileRequestDto;
import com.sparta.springtrello.domain.user.entity.User;
import com.sparta.springtrello.domain.user.service.KakaoService;
import com.sparta.springtrello.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<HttpResponseDto<Void>> signup(@Validated @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseUtils.success(HttpStatus.CREATED);
    }

    // 프로필 업로드
    @PutMapping("/profile")
    public ResponseEntity<HttpResponseDto<Void>> updateProfile(
            @Validated @RequestPart("updateProfileRequestDto") UpdateProfileRequestDto requestDto,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        userService.updateProfile(user, requestDto, profilePicture);
        return ResponseUtils.success(HttpStatus.OK);
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<HttpResponseDto<Void>> updatePassword(
            @AuthenticationPrincipal UserDetailsImpl loginUser,
            @Validated @RequestBody UpdatePasswordRequestDto requestDto
    ) {
        userService.updatePassword(loginUser.getUser(), requestDto);
        return ResponseUtils.success(HttpStatus.OK);
    }

    // 프로필 조회
    @GetMapping("/{userId}")
    public ResponseEntity<HttpResponseDto<ProfileResponseDto>> getProfile(@PathVariable Long userId) {
        ProfileResponseDto profile = userService.getProfile(userId);
        return ResponseUtils.success(HttpStatus.OK, profile);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<HttpResponseDto<Void>> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.logout(userDetails.getUser());
        SecurityContextHolder.clearContext();
        return ResponseUtils.success(HttpStatus.OK);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<HttpResponseDto<Void>> deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        userService.deleteUser(user);
        SecurityContextHolder.clearContext();  // 보안 컨텍스트 초기화
        return ResponseUtils.success(HttpStatus.OK);
    }

    // 카카오 로그인 요청
    @GetMapping("/kakao/authorize")
    public void redirectToKakaoAuthorize(HttpServletResponse response) throws IOException {
        String requestUrl = String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                clientId, redirectUri
        );
        response.sendRedirect(requestUrl);
    }

    // 카카오 콜백 처리
    @GetMapping("/kakao/callback")
    public ResponseEntity<HttpResponseDto<Void>> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        String token = kakaoService.kakaoLogin(code);
        log.info("Generated token: " + token);
        // Bearer 접두사 확인 및 추가
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }
        response.setHeader(JwtProvider.AUTHORIZATION_HEADER, token);
        return ResponseUtils.success(HttpStatus.OK);
    }
}
