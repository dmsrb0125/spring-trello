package com.sparta.springtrello.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpResponseDto<T> {
    private Integer statusCode;
    private String message;
    private T data;

    // 상태코드 ,메세지, 데이터를 선택적으로 응답
    public static <T> HttpResponseDto<T> of(HttpStatus status, String message, T data) {
        return HttpResponseDto.<T>builder()
                .statusCode(status.value())
                .message(message)
                .data(data)
                .build();
    }
}
