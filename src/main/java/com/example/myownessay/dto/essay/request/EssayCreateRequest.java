package com.example.myownessay.dto.essay.request;

import com.example.myownessay.entity.enums.EssayTheme;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EssayCreateRequest {

    private String title;

    @NotBlank(message = "에세이 내용은 필수입니다.")
    private String finalContent;

    private EssayTheme theme;

    private String coverImage;

    @NotNull(message = "주 시작 날짜는 필수입니다.")
    private LocalDate weekStart;

    @NotNull(message = "주 종료 날짜는 필수입니다.")
    private LocalDate weekEnd;

    private String aiDraft;
}
