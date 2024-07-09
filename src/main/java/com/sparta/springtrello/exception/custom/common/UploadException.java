package com.sparta.springtrello.exception.custom.common;

import com.sparta.springtrello.common.ResponseCodeEnum;
import com.sparta.springtrello.exception.CustomException;

public class UploadException extends CustomException {
    public UploadException(ResponseCodeEnum responseCode) {
        super(responseCode);
    }
}