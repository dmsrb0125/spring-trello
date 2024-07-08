package com.sparta.springtrello.exception.custom.auth;

import com.sparta.springtrello.common.ResponseCodeEnum;

public class InvalidTokenException extends UserAuthenticationException {
    public InvalidTokenException() {
        super(ResponseCodeEnum.INVALID_TOKENS);
    }
}
