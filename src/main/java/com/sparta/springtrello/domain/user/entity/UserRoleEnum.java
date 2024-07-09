package com.sparta.springtrello.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    ROLE_USER("USER"),  // 사용자 권한
    ROLE_MANAGER("MANAGER"),  // 매니저 권한
    ;

    private final String role;

    UserRoleEnum(String role) {
        this.role = role;
    }
}