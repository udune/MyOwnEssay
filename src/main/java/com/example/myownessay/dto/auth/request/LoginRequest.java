package com.example.myownessay.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message="이메일은 필수 입력 항목입니다.")
    @Email(message="유효한 이메일 주소를 입력해주세요.")
    @Size(max = 100, message="이메일은 100자 이하로 입력해주세요.")
    private String email;

    @NotBlank(message="비밀번호는 필수 입력 항목입니다.")
    @Size(min = 6, max = 20, message="비밀번호는 6자 이상 20자 이하로 입력해주세요.")
    private String password;

}
