package com.sparta.springtrello.exception.custom.common;

import com.sparta.springtrello.common.ResponseCodeEnum;
import lombok.Getter;


@Getter
public class UploadException extends RuntimeException {
    private final ResponseCodeEnum responseCode;

    public UploadException(ResponseCodeEnum responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}