package com.sparta.springtrello.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCodeEnum {
    // 유저 관련 예외
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인 실패"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자명입니다."),
    USER_DELETED(HttpStatus.UNAUTHORIZED, "탈퇴한 사용자입니다"),
    INVALID_TOKENS(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다"),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호가 이전 비밀번호와 동일합니다."),
    INVALID_MANAGER_PASSWORD(HttpStatus.FORBIDDEN, "관리자 암호가 틀렸습니다."),

    // 공용 예외
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
