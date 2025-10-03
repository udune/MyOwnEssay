package com.example.myownessay.repository;

import com.example.myownessay.entity.Record;
import com.example.myownessay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    // 특정 사용자와 날짜에 해당하는 모든 기록을 조회
    List<Record> findByUserAndRecordDate(User user, LocalDate recordDate);

}
