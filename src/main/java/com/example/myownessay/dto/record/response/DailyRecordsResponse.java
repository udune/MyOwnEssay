package com.example.myownessay.dto.record.response;

import com.example.myownessay.entity.enums.SlotType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyRecordsResponse {

    // 해당 날짜
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    // 해당 날짜의 모든 기록
    private List<RecordResponse> records;

    // 완료율 (0.0 ~ 1.0)
    private Double completionRate;

    // 완료된 슬롯 수
    private Integer completedCount;

    // 총 슬롯 수는 고정값 4로 설정
    private Integer totalSlots;

    // 모든 슬롯이 완료되었는지 여부
    private Boolean isAllCompleted;

    // 완료되지 않은 슬롯 타입 목록
    private List<String> incompleteRecords;

    public static DailyRecordsResponse from(LocalDate date, List<RecordResponse> records, double completionRate, int completedCount, boolean isAllCompleted) {
        // 완료된 기록의 슬롯 타입 이름 목록 생성
        List<String> completedRecordTypes = records.stream()
                .filter(RecordResponse::getIsCompleted)
                .map(record -> record.getSlotType().name())
                .toList();

        // 완료되지 않은 슬롯 타입 이름 목록 생성
        List<String> incompleteRecords = Arrays.stream(SlotType.values())
                .map(SlotType::name)
                .filter(record -> !completedRecordTypes.contains(record))
                .toList();

        return DailyRecordsResponse.builder()
                .date(date)
                .records(records)
                .completionRate(completionRate)
                .completedCount(completedCount)
                .totalSlots(4)
                .isAllCompleted(isAllCompleted)
                .incompleteRecords(incompleteRecords)
                .build();
    }

    public Integer getCompletionPercentage() {
        return completionRate != null ? (int) Math.round(completionRate * 100) : null;
    }
}
