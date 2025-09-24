package com.example.myownessay.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAccountRequest {

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

}
