package com.sparta.springtrello.exception.custom.user;

import com.sparta.springtrello.common.ResponseCodeEnum;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException() {
        super(ResponseCodeEnum.USER_ALREADY_EXISTS);
    }
}