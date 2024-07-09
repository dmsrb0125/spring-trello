package com.sparta.springtrello.domain.user.repository;

import com.sparta.springtrello.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.sparta.springtrello.exception.custom.user.UserException;
import com.sparta.springtrello.common.ResponseCodeEnum;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdapter {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(ResponseCodeEnum.USER_NOT_FOUND));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(ResponseCodeEnum.USER_NOT_FOUND));
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User createKakaoUser(Long kakaoId, String nickname, String pictureUrl) {
        return userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);
            User newUser = new User(kakaoId, "kakao_" + kakaoId, pictureUrl, encodedPassword);
            newUser.setNickname(nickname);
            userRepository.save(newUser);
            return newUser;
        });
    }
}
