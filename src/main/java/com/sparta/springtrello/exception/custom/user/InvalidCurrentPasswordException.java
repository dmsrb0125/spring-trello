package com.sparta.springtrello.exception.custom.user;

import com.sparta.springtrello.common.ResponseCodeEnum;

public class InvalidCurrentPasswordException extends UserException {
    public InvalidCurrentPasswordException() {
        super(ResponseCodeEnum.INVALID_CURRENT_PASSWORD);
    }
}