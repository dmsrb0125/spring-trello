package com.sparta.springtrello.exception;

import com.sparta.springtrello.common.HttpResponseDto;
import com.sparta.springtrello.common.ResponseUtils;
import com.sparta.springtrello.exception.custom.user.UserException;
import com.sparta.springtrello.exception.custom.common.UploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<HttpResponseDto<Void>> handleUserException(UserException e) {
        log.error("에러 메세지: ", e);
        return ResponseUtils.error(e.getResponseCode());
    }

    @ExceptionHandler(UploadException.class)
    public ResponseEntity<HttpResponseDto<Void>> handleUploadException(UploadException e) {
        log.error("파일 업로드 에러: ", e);
        return ResponseUtils.error(e.getResponseCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpResponseDto<List<String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errorMessageList = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach(v -> errorMessageList.add(v.getDefaultMessage()));
        log.error("유효성 검사 실패:\n\n{}", String.join(",\n", errorMessageList));
        return ResponseUtils.success(HttpStatus.BAD_REQUEST, errorMessageList);
    }
}
