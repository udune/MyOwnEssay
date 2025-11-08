package com.example.myownessay.dto;

import com.example.myownessay.entity.WeekProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 주간 진행도 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekProgressResponse {

    /**
     * 주의 시작 날짜 (월요일, yyyy-MM-dd 형식)
     */
    private LocalDate weekStart;

    /**
     * 완료된 일수 (0-7)
     */
    private Integer completedDays;

    /**
     * 완료율 (0.0 ~ 1.0)
     */
    private Double completionRate;

    /**
     * 에세이 생성 가능 여부
     */
    private Boolean canGenerateEssay;

    /**
     * 에세이 생성 여부
     */
    private Boolean essayGenerated;

    /**
     * WeekProgress 엔티티로부터 DTO 생성
     * @param weekProgress 주간 진행도 엔티티
     * @return WeekProgressResponse
     */
    public static WeekProgressResponse from(WeekProgress weekProgress) {
        return WeekProgressResponse.builder()
                .weekStart(weekProgress.getWeekStart())
                .completedDays(weekProgress.getCompletedDays())
                .completionRate(weekProgress.getCompletionRate())
                .canGenerateEssay(weekProgress.canGenerateEssay())
                .essayGenerated(weekProgress.getEssayGenerated())
                .build();
    }
}
