package com.example.myownessay.dto.record.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RecordRequest {

    @NotNull(message="기록 내용은 필수입니다.")
    private Map<String, Object> content;

    @NotNull(message="완료 여부는 필수입니다.")
    private Boolean completed;

}
