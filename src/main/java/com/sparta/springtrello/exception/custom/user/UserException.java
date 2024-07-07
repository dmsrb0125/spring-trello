package com.sparta.springtrello.exception.custom.user;


import com.sparta.springtrello.common.ResponseCodeEnum;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    private final ResponseCodeEnum responseCode;

    public UserException(ResponseCodeEnum responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}
