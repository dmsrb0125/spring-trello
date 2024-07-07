package com.sparta.springtrello.domain.user.service;


import com.sparta.springtrello.domain.user.dto.ProfileResponseDto;
import com.sparta.springtrello.domain.user.dto.SignupRequestDto;
import com.sparta.springtrello.domain.user.entity.User;
import com.sparta.springtrello.domain.user.entity.UserStatusEnum;
import com.sparta.springtrello.domain.user.repository.UserAdapter;
import com.sparta.springtrello.exception.custom.user.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    private final UserAdapter userAdapter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserAdapter userAdapter, PasswordEncoder passwordEncoder) {
        this.userAdapter = userAdapter;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    @Transactional
    public void signup(SignupRequestDto requestDto) {

        // 사용자명이 이미 존재하는지 확인
        if (userAdapter.existsByUsername(requestDto.getUsername())) {
            throw new UserAlreadyExistsException();
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(
                requestDto.getUsername(),
                encodedPassword,
                UserStatusEnum.STATUS_NORMAL
        );

        userAdapter.save(user);
    }

    // 프로필 조회
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long userId) {
        User user = userAdapter.findById(userId);
        return new ProfileResponseDto(user.getNickname(), user.getIntroduce(), user.getPictureUrl());
    }

   // 회원 탈퇴
   @Transactional
   public void deleteUser(User user) {
        user.setUserStatus(UserStatusEnum.STATUS_DELETED);
        user.setRefreshToken(null);
        userAdapter.save(user);
   }
}