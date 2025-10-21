package com.example.myownessay.service;


import org.springframework.stereotype.Service;

@Service
public class RecordCompletionService {

    // 하루 완료율 계산 메서드
    public double calculateDailyCompletion(int completedCount) {
        if (completedCount < 0 || completedCount > 4) {
            throw new IllegalArgumentException("완료된 기록 수는 0에서 4 사이여야 합니다.");
        }

        return (double) completedCount / 4;
    }

    // 주간 완료율 계산 메서드
    public double calculateWeeklyCompletion(int completedDays) {
        if (completedDays < 0 || completedDays > 7) {
            throw new IllegalArgumentException("완료된 일수는 0에서 7 사이여야 합니다.");
        }

        return (double) completedDays / 7;
    }

    // 모든 기록이 완료되었는지 확인하는 메서드
    public boolean isAllCompleted(int completedCount) {
        return completedCount == 4;
    }

    // 완료율을 백분율로 변환하는 메서드
    public int toPercentage(double completionRate) {
        return (int) Math.round(completionRate * 100);
    }

}
