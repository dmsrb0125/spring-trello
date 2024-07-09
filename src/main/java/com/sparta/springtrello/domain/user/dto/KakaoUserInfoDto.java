package com.sparta.springtrello.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoDto {
    private Long id;
    private String nickname;
    private String pictureUrl;

    public KakaoUserInfoDto(Long id, String nickname, String pictureUrl) {
        this.id = id;
        this.nickname = nickname;
        this.pictureUrl = pictureUrl;
    }
}