package com.sparta.springtrello.exception.custom.user;


import com.sparta.springtrello.common.ResponseCodeEnum;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super(ResponseCodeEnum.USER_NOT_FOUND);
    }
}
