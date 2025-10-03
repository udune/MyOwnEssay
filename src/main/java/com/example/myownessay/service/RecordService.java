package com.example.myownessay.service;

import com.example.myownessay.dto.record.RecordDto;
import com.example.myownessay.dto.record.response.DailyRecordsResponse;
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
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    // 특정 사용자의 특정 날짜에 해당하는 모든 기록을 조회하고, 완료율을 계산하여 반환
    @Transactional(readOnly = true)
    public DailyRecordsResponse getDailyRecords(String email, LocalDate date) {
        log.info("일일 기록 조회 요청 - 이메일: {}, 날짜: {}", email, date);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Record> records = recordRepository.findByUserAndRecordDate(user, date);
        log.info("조회된 기록 수: {}", records.size());

        List<RecordDto> recordDtos = records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        double completionRate = calculateCompletionRate(records);

        log.info("완료율: {}%", completionRate * 100);
        return new DailyRecordsResponse(date, recordDtos, completionRate);
    }

    // Record 엔티티를 RecordDto로 변환
    private RecordDto convertToDto(Record record) {
        RecordDto dto = new RecordDto();
        dto.setSlotType(record.getSlotType());
        dto.setContent(record.getContent());
        dto.setCompleted(record.getIsCompleted());
        dto.setCompletedAt(record.getCreatedAt());
        return dto;
    }

    // 완료된 기록의 비율 계산
    private double calculateCompletionRate(List<Record> records) {
        if (records.isEmpty()) {
            return 0.0;
        }

        long completedCount = records.stream()
                .filter(Record::getIsCompleted)
                .count();

        return (double) completedCount / records.size();
    }
}
