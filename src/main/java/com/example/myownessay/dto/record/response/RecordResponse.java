package com.example.myownessay.dto.record.response;

import com.example.myownessay.entity.Record;
import com.example.myownessay.entity.enums.SlotType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    private SlotType slotType;

    private String slotTypeDescription;

    private Map<String, Object> content;

    private Boolean isCompleted;

    private Boolean isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    // Entity -> DTO 변환 메서드
    public static RecordResponse from(Record record) {
        return RecordResponse.builder()
                .id(record.getId())
                .recordDate(record.getRecordDate())
                .slotType(record.getSlotType())
                .slotTypeDescription(record.getSlotType().getDescription())
                .content(record.getContent())
                .isCompleted(record.getIsCompleted())
                .isDeleted(record.getIsDeleted())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .deletedAt(record.getDeletedAt())
                .build();
    }

    // 오늘 날짜인지 확인하는 메서드
    public boolean isToday() {
        return recordDate != null && recordDate.equals(LocalDate.now());
    }

    // 과거 날짜인지 확인하는 메서드
    public boolean isPart() {
        return recordDate != null && recordDate.isBefore(LocalDate.now());
    }

    // 미래 날짜인지 확인하는 메서드
    public boolean isFuture() {
        return recordDate != null && recordDate.isAfter(LocalDate.now());
    }
}
