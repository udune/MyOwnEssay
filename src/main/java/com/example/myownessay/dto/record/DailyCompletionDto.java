package com.example.myownessay.dto.record;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 일일 완료 통계 정보를 담는 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyCompletionDto {
    private int completedCount; // 완료된 기록 수
    private int totalRecords; // 총 기록 수
    private double completionRate; // 완료율 (0.0 ~ 1.0)
    private boolean isAllCompleted; // 모든 기록이 완료되었는지 여부

    public int getPercentage() {
        return (int) Math.round(completionRate * 100);
    }
}
