package com.example.myownessay.service;

import com.example.myownessay.dto.WeekProgressResponse;
import com.example.myownessay.entity.Record;
import com.example.myownessay.entity.User;
import com.example.myownessay.entity.WeekProgress;
import com.example.myownessay.repository.RecordRepository;
import com.example.myownessay.repository.UserRepository;
import com.example.myownessay.repository.WeekProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 주간 진행도 서비스
 * 주간 기록 완료 현황을 계산하고 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeekProgressService {

    private final WeekProgressRepository weekProgressRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    /**
     * 특정 주의 진행도 조회
     * 진행도가 없으면 새로 계산하여 생성합니다.
     *
     * @param email 사용자 이메일
     * @param weekStart 주 시작 날짜 (월요일)
     * @return 주간 진행도 응답
     */
    @Transactional
    public WeekProgressResponse getWeekProgress(String email, LocalDate weekStart) {
        log.info("주간 진행도 조회 - 이메일: {}, 주 시작: {}", email, weekStart);

        // 월요일인지 검증
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("주 시작 날짜는 월요일이어야 합니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 주간 진행도 조회 또는 생성
        WeekProgress weekProgress = weekProgressRepository.findByUserAndWeekStart(user, weekStart)
                .orElseGet(() -> calculateAndSaveWeekProgress(user, weekStart));

        return WeekProgressResponse.from(weekProgress);
    }

    /**
     * 주간 진행도를 계산하고 저장합니다.
     *
     * @param user 사용자
     * @param weekStart 주 시작 날짜 (월요일)
     * @return 생성된 주간 진행도
     */
    @Transactional
    public WeekProgress calculateAndSaveWeekProgress(User user, LocalDate weekStart) {
        log.info("주간 진행도 계산 및 저장 - 사용자 ID: {}, 주 시작: {}", user.getId(), weekStart);

        // 주 시작과 종료 날짜 계산
        LocalDate weekEnd = weekStart.plusDays(6); // 일요일

        // 해당 주의 모든 기록 조회
        List<Record> weekRecords = recordRepository.findByUserAndRecordDateBetween(
                user, weekStart, weekEnd
        );

        // 완료된 기록이 있는 날짜 집합 (삭제되지 않고 완료된 기록만)
        Set<LocalDate> completedDates = weekRecords.stream()
                .filter(record -> !record.getIsDeleted() && record.isCompleted())
                .map(Record::getRecordDate)
                .collect(Collectors.toSet());

        int completedDays = completedDates.size();

        log.info("계산된 완료 일수: {} (총 {} 건의 완료 기록)", completedDays, weekRecords.size());

        // 기존 WeekProgress 조회 또는 새로 생성
        WeekProgress weekProgress = weekProgressRepository.findByUserAndWeekStart(user, weekStart)
                .orElse(WeekProgress.builder()
                        .user(user)
                        .weekStart(weekStart)
                        .completedDays(0)
                        .essayGenerated(false)
                        .build());

        // 완료 일수 업데이트
        weekProgress.updateCompletedDays(completedDays);

        return weekProgressRepository.save(weekProgress);
    }

    /**
     * 특정 날짜가 속한 주의 진행도를 업데이트합니다.
     * 기록이 변경될 때마다 호출되어야 합니다.
     *
     * @param user 사용자
     * @param date 기록 날짜
     */
    @Transactional
    public void updateWeekProgressForDate(User user, LocalDate date) {
        LocalDate weekStart = getWeekStart(date);
        calculateAndSaveWeekProgress(user, weekStart);
        log.info("날짜 {}에 대한 주간 진행도 업데이트 완료 (주 시작: {})", date, weekStart);
    }

    /**
     * 현재 주의 진행도 조회
     *
     * @param email 사용자 이메일
     * @return 주간 진행도 응답
     */
    @Transactional
    public WeekProgressResponse getCurrentWeekProgress(String email) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = getWeekStart(today);
        return getWeekProgress(email, weekStart);
    }

    /**
     * 에세이 생성 완료 표시
     *
     * @param email 사용자 이메일
     * @param weekStart 주 시작 날짜
     */
    @Transactional
    public void markEssayGenerated(String email, LocalDate weekStart) {
        log.info("에세이 생성 완료 표시 - 이메일: {}, 주 시작: {}", email, weekStart);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        WeekProgress weekProgress = weekProgressRepository.findByUserAndWeekStart(user, weekStart)
                .orElseThrow(() -> new RuntimeException("주간 진행도를 찾을 수 없습니다."));

        if (!weekProgress.canGenerateEssay()) {
            throw new IllegalStateException("에세이 생성 조건을 만족하지 않습니다. (최소 3일 필요)");
        }

        weekProgress.markEssayGenerated();
        weekProgressRepository.save(weekProgress);

        log.info("에세이 생성 완료 표시 성공");
    }

    /**
     * 사용자의 모든 주간 진행도 조회 (최신순)
     *
     * @param email 사용자 이메일
     * @return 주간 진행도 리스트
     */
    @Transactional(readOnly = true)
    public List<WeekProgressResponse> getAllWeekProgress(String email) {
        log.info("모든 주간 진행도 조회 - 이메일: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<WeekProgress> weekProgressList = weekProgressRepository.findByUserOrderByWeekStartDesc(user);

        return weekProgressList.stream()
                .map(WeekProgressResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜가 속한 주의 월요일 날짜를 계산합니다.
     *
     * @param date 날짜
     * @return 해당 주의 월요일 날짜
     */
    public static LocalDate getWeekStart(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int daysFromMonday = dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(daysFromMonday);
    }
}
