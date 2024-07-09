package com.sparta.springtrello.exception.custom.user;

import com.sparta.springtrello.common.ResponseCodeEnum;
import com.sparta.springtrello.exception.CustomException;

public class PasswordException extends CustomException {
    public PasswordException(ResponseCodeEnum responseCode) {
        super(responseCode);
    }
}

