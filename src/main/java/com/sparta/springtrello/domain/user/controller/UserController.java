package com.sparta.springtrello.domain.user.controller;

import com.sparta.springtrello.auth.UserDetailsImpl;
import com.sparta.springtrello.common.HttpResponseDto;
import com.sparta.springtrello.common.ResponseUtils;
import com.sparta.springtrello.domain.user.dto.ProfileResponseDto;
import com.sparta.springtrello.domain.user.dto.SignupRequestDto;
import com.sparta.springtrello.domain.user.entity.User;
import com.sparta.springtrello.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<HttpResponseDto<Void>> signup(@Validated @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseUtils.success(HttpStatus.CREATED);
    }

    // 프로필 조회
    @GetMapping("/{userId}")
    public ResponseEntity<HttpResponseDto<ProfileResponseDto>> getProfile(@PathVariable Long userId) {
        ProfileResponseDto profile = userService.getProfile(userId);
        return ResponseUtils.success(HttpStatus.OK, profile);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<HttpResponseDto<Void>> deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        userService.deleteUser(user);
        SecurityContextHolder.clearContext();  // 보안 컨텍스트 초기화
        return ResponseUtils.success(HttpStatus.OK);
    }
}
