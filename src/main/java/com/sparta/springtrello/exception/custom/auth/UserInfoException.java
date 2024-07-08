package com.sparta.springtrello.exception.custom.auth;

import com.sparta.springtrello.common.ResponseCodeEnum;

public class UserInfoException extends UserAuthenticationException {
    public UserInfoException() {
        super(ResponseCodeEnum.INVALID_USER_INFO);
    }
}
