package com.sparta.springtrello.domain.user.service;

import com.sparta.springtrello.common.S3Uploader;
import com.sparta.springtrello.domain.user.dto.ProfileResponseDto;
import com.sparta.springtrello.domain.user.dto.SignupRequestDto;
import com.sparta.springtrello.domain.user.dto.UpdatePasswordRequestDto;
import com.sparta.springtrello.domain.user.dto.UpdateProfileRequestDto;
import com.sparta.springtrello.domain.user.entity.User;
import com.sparta.springtrello.domain.user.entity.UserStatusEnum;
import com.sparta.springtrello.domain.user.repository.UserAdapter;
import com.sparta.springtrello.exception.custom.user.UserException;
import com.sparta.springtrello.exception.custom.user.PasswordException;
import com.sparta.springtrello.common.ResponseCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {

    private final UserAdapter userAdapter;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    @Autowired
    public UserService(UserAdapter userAdapter, PasswordEncoder passwordEncoder, S3Uploader s3Uploader) {
        this.userAdapter = userAdapter;
        this.passwordEncoder = passwordEncoder;
        this.s3Uploader = s3Uploader;
    }

    // 회원가입
    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 사용자명이 이미 존재하는지 확인
        if (userAdapter.existsByUsername(requestDto.getUsername())) {
            throw new UserException(ResponseCodeEnum.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(
                requestDto.getUsername(),
                encodedPassword,
                UserStatusEnum.STATUS_NORMAL
        );

        userAdapter.save(user);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(User loginUser, UpdatePasswordRequestDto requestDto) {
        User user = userAdapter.findById(loginUser.getId());

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new PasswordException(ResponseCodeEnum.PASSWORD_INCORRECT);
        }

        // 새로운 비밀번호가 현재 비밀번호와 다른지 확인
        if (passwordEncoder.matches(requestDto.getNewPassword(), user.getPassword())) {
            throw new PasswordException(ResponseCodeEnum.SAME_AS_OLD_PASSWORD);
        }

        // 비밀번호 업데이트
        String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());
        user.setPassword(encodedNewPassword);
        userAdapter.save(user);
    }

    // 프로필 업로드
    @Transactional
    public void updateProfile(User user, UpdateProfileRequestDto requestDto, MultipartFile profilePicture) {
        user.setNickname(requestDto.getNickname());
        user.setIntroduce(requestDto.getIntroduce());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                String pictureUrl = s3Uploader.upload(profilePicture, "profile-pictures");
                user.setPictureUrl(pictureUrl);
            } catch (IOException e) {
                throw new UserException(ResponseCodeEnum.UPLOAD_FAILED);
            }
        }
        userAdapter.save(user);
    }

    // 프로필 조회
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long userId) {
        User user = userAdapter.findById(userId);
        return new ProfileResponseDto(user.getNickname(), user.getIntroduce(), user.getPictureUrl());
    }

    // 로그아웃
    @Transactional
    public void logout(User user) {
        user.setRefreshToken(null);  // 리프레시 토큰을 무효화
        userAdapter.save(user);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(User user) {
        user.setUserStatus(UserStatusEnum.STATUS_DELETED);
        user.setRefreshToken(null);
        userAdapter.save(user);
    }
}
