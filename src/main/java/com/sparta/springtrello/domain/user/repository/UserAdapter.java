package com.sparta.springtrello.domain.user.repository;


import com.sparta.springtrello.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.sparta.springtrello.exception.custom.user.UserNotFoundException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdapter {
    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public void save(User user) {
        userRepository.save(user);
    }
}
