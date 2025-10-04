package com.example.myownessay.dto.record.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
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
    private List<String> incompleteSlots;

    // Factory method to create DailyRecordsResponse
    public static DailyRecordsResponse from(LocalDate date, List<RecordResponse> records) {
        int totalSlots = 4;
        long completedCount = records.stream()
                .filter(RecordResponse::getIsCompleted)
                .count();

        double completionRate = totalSlots > 0
                ? (double) completedCount / totalSlots
                : 0.0;

        boolean isAllCompleted = completedCount == totalSlots;

        List<String> allSlots = List.of("READING", "CONSULTING", "HEALING", "DIARY");
        List<String> completedSlotTypes = records.stream()
                .filter(RecordResponse::getIsCompleted)
                .map(record -> record.getSlotType().name())
                .toList();

        List<String> incompleteSlots = allSlots.stream()
                .filter(slot -> !completedSlotTypes.contains(slot))
                .toList();

        return DailyRecordsResponse.builder()
                .date(date)
                .records(records)
                .completionRate(completionRate)
                .completedCount((int) completedCount)
                .totalSlots(totalSlots)
                .isAllCompleted(isAllCompleted)
                .incompleteSlots(incompleteSlots)
                .build();
    }
}
