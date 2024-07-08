package com.sparta.springtrello.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class ResponseUtils {

    // 에러 상황일 -> 상태 코드, 메시지 응답(이넘 사용)
    public static ResponseEntity<HttpResponseDto<Void>> error(ResponseCodeEnum responseCodeEnum) {
        return ResponseEntity.status(responseCodeEnum.getHttpStatus())
                .body(HttpResponseDto.of(
                        responseCodeEnum.getHttpStatus(), responseCodeEnum.getMessage(), null
                ));
    }

    // 일반적 성공 상황일 때 -> 상태 코드와 메시지 응답
    public static ResponseEntity<HttpResponseDto<Void>> success(HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus)
                .body(HttpResponseDto.of(
                        httpStatus, "success", null
                ));
    }

    // 조회 요청 성공 상황일 때 -> 상태 코드, 메시지, 조회할 데이터 응답
    public static <T> ResponseEntity<HttpResponseDto<T>> success(HttpStatus httpStatus, T data) {
        return ResponseEntity.status(httpStatus)
                .body(HttpResponseDto.of(
                        httpStatus, "success", data
                ));
    }
}
