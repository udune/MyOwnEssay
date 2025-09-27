package com.example.myownessay.dto.auth.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣]+$",
            message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다."
    )
    private String nickname;

    @Size(max = 50, message = "타임존은 50자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^[a-zA-Z/_]*$",
            message = "타임존은 영문과 슬래시(_/)만 사용할 수 있습니다."
    )
    private String timezone;

}
