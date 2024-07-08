package com.sparta.springtrello.exception.custom.auth;

import com.sparta.springtrello.common.ResponseCodeEnum;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class UserAuthenticationException extends AuthenticationException {
    private final ResponseCodeEnum responseCode;

    public UserAuthenticationException(ResponseCodeEnum responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}
