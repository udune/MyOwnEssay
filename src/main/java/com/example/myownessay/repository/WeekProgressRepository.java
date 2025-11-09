package com.example.myownessay.repository;

import com.example.myownessay.entity.User;
import com.example.myownessay.entity.WeekProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 주간 진행도 리포지토리
 */
@Repository
public interface WeekProgressRepository extends JpaRepository<WeekProgress, Long> {

    /**
     * 특정 사용자와 주 시작 날짜로 주간 진행도 조회
     * @param user 사용자
     * @param weekStart 주 시작 날짜 (월요일)
     * @return 주간 진행도
     */
    Optional<WeekProgress> findByUserAndWeekStart(User user, LocalDate weekStart);

    /**
     * 특정 사용자의 모든 주간 진행도 조회 (최신순)
     * @param user 사용자
     * @return 주간 진행도 리스트
     */
    List<WeekProgress> findByUserOrderByWeekStartDesc(User user);

    /**
     * 특정 사용자의 날짜 범위 내 주간 진행도 조회
     * @param user 사용자
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 주간 진행도 리스트
     */
    List<WeekProgress> findByUserAndWeekStartBetweenOrderByWeekStartDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 특정 사용자의 에세이가 아직 생성되지 않은 주간 진행도 조회
     * @param user 사용자
     * @return 주간 진행도 리스트
     */
    List<WeekProgress> findByUserAndEssayGeneratedFalseOrderByWeekStartDesc(User user);
}
