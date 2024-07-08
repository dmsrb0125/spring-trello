package com.sparta.springtrello.exception.custom.auth;

import com.sparta.springtrello.common.ResponseCodeEnum;

public class UserDeletedException extends UserAuthenticationException {
    public UserDeletedException() {
        super(ResponseCodeEnum.USER_DELETED);
    }
}
