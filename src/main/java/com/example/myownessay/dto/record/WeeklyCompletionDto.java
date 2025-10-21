package com.example.myownessay.dto.record;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 주간 완료율을 나타내는 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyCompletionDto {
    private int completedDays; // 완료된 일수
    private int totalDays; // 총 일수 (보통 7)
    private double completionRate; // 완료율 (0.0 ~ 1.0)

    public int getPercentage() {
        return (int) Math.round(completionRate * 100);
    }
}
