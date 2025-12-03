package com.example.myownessay.dto.essay.request;

import com.example.myownessay.entity.enums.EssayTheme;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EssayUpdateRequest {

    private String title;

    private String finalContent;

    private EssayTheme theme;

    private String coverImage;
}
