package com.example.myownessay.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

    private Long id;
    private String email;
    private String nickname;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
