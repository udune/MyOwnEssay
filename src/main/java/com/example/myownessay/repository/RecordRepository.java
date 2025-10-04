package com.example.myownessay.repository;

import com.example.myownessay.entity.Record;
import com.example.myownessay.entity.User;
import com.example.myownessay.entity.enums.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    // 특정 사용자와 날짜에 해당하는 모든 기록을 조회
    List<Record> findByUserAndRecordDate(User user, LocalDate recordDate);

    // 특정 사용자, 날짜, 슬롯 타입에 해당하는 기록을 조회
    Optional<Record> findByUserAndRecordDateAndSlotType(
            User user,
            LocalDate recordDate,
            SlotType slotType
    );

    // 특정 사용자와 날짜 범위에 해당하는 모든 기록을 조회
    List<Record> findByUserAndRecordDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
