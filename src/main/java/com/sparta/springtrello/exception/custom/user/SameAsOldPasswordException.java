package com.sparta.springtrello.exception.custom.user;

import com.sparta.springtrello.common.ResponseCodeEnum;

public class SameAsOldPasswordException extends UserException {
    public SameAsOldPasswordException() {
        super(ResponseCodeEnum.SAME_AS_OLD_PASSWORD);
    }
}