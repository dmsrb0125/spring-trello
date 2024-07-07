package com.sparta.springtrello.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    STATUS_NORMAL("NORMAL"), // 일반 상태
    STATUS_DELETED("DELETED"), // 탈퇴한 상태
    ;

    private final String status;

    UserStatusEnum(String status) {
        this.status = status;
    }
}