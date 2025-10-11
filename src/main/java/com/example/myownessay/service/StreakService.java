package com.example.myownessay.service;

import com.example.myownessay.entity.User;
import com.example.myownessay.entity.Record;
import com.example.myownessay.repository.RecordRepository;
import com.example.myownessay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakService {
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    // 현재 연속 기록 조회
    @Transactional(readOnly = true)
    public int getCurrentStreak(String email) {
        log.info("현재 연속 기록 조회 - 이메일: {}", email);

        // 사용자 조회
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 오늘 날짜
        LocalDate today = LocalDate.now();
        int streak = 0; // 연속 기록 초기화

        // 연속 기록 계산
        for (int i = 0; i < 365; i++) {
            // 오늘부터 과거로 하루씩 체크
            LocalDate checkDate = today.minusDays(i);

            if (hasCompletedRecordOnDate(user, checkDate)) {
                streak++; // 완료된 기록이 있으면 연속 기록 증가
            } else {
                break; // 연속 기록이 끊긴 경우 루프 종료
            }
        }

        log.info("현재 연속 기록: {}일", streak);
        return streak;
    }

    // 최대 연속 기록 조회
    @Transactional(readOnly = true)
    public int getMaxStreak(String email) {
        log.info("최대 연속 기록 조회 - 이메일: {}", email);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 1년치 기록 조회
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now();

        // 해당 기간 동안의 모든 기록 조회
        List<Record> records = recordRepository.findByUserAndRecordDateBetween(user, startDate, endDate);

        // 날짜별로 완료된 기록이 있는지 체크
        List<LocalDate> completedDates = records.stream()
                .filter(Record::isCompleted)
                .map(Record::getRecordDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (completedDates.isEmpty()) {
            return 0; // 완료된 기록이 없으면 최대 연속 기록은 0
        }

        int maxStreak = 1; // 최대 연속 기록 초기화
        int currentStreak = 1;

        // 연속된 날짜 계산
        for (int i = 1; i < completedDates.size(); i++) {
            LocalDate prev = completedDates.get(i - 1); // 이전 날짜
            LocalDate current = completedDates.get(i); // 현재 날짜

            if (prev.plusDays(1).equals(current)) {
                currentStreak++; // 연속된 날짜이면 현재 연속 기록 증가
                maxStreak = Math.max(maxStreak, currentStreak); // 최대 연속 기록 갱신
            } else {
                currentStreak = 1; // 현재 연속 기록 초기화
            }
        }

        log.info("최대 연속 기록: {}일", maxStreak);
        return maxStreak;
    }

    // 특정 날짜에 사용자가 완료한 기록이 있는지 확인
    private boolean hasCompletedRecordOnDate(User user, LocalDate date) {
        // 해당 날짜에 사용자의 모든 기록 조회
        List<Record> records = recordRepository.findByUserAndRecordDate(user, date);

        // 완료된 기록이 하나라도 있는지 확인
        return records.stream().anyMatch(Record::isCompleted);
    }
}
