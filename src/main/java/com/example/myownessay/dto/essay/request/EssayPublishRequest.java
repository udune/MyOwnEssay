package com.example.myownessay.dto.essay.request;

import com.example.myownessay.entity.enums.PublishStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EssayPublishRequest {

    @NotNull(message = "공개 상태는 필수입니다.")
    private PublishStatus status;
}
