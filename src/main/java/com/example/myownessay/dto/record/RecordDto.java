package com.example.myownessay.dto.record;

import com.example.myownessay.entity.enums.SlotType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordDto {

    private SlotType slotType;
    private Map<String, Object> content;
    private Boolean completed;
    private LocalDateTime completedAt;

}
