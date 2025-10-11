package com.example.myownessay.dto.streak.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {
    private int currentStreak; // 현재 연속 기록
    private int maxStreak; // 최대 연속 기록

    // 정적 팩토리 메서드
    public static StreakResponse of(int currentStreak, int maxStreak) {
        return new StreakResponse(currentStreak, maxStreak);
    }
}
