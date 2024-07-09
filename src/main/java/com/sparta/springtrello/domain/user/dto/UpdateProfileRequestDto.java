package com.sparta.springtrello.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDto {

    @Size(max = 10, message = "이름은 최대 10글자까지 입력 가능합니다.")
    private String nickname;

    @Size(max = 20, message = "한줄소개는 최대 20글자까지 입력 가능합니다.")
    private String introduce;
}
