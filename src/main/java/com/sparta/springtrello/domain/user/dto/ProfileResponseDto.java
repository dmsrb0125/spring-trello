package com.sparta.springtrello.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponseDto {
    private String nickname;
    private String introduce;
    private String pictureUrl;
}