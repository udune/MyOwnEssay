package com.example.myownessay.service;

import com.example.myownessay.dto.record.request.RecordRequest;
import com.example.myownessay.dto.record.response.DailyRecordsResponse;
import com.example.myownessay.dto.record.response.RecordResponse;
import com.example.myownessay.entity.User;
import com.example.myownessay.entity.Record;
import com.example.myownessay.entity.enums.SlotType;
import com.example.myownessay.repository.RecordRepository;
import com.example.myownessay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// 기록 관련 비즈니스 로직을 처리하는 서비스 클래스
@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    // 특정 사용자의 특정 날짜와 슬롯 타입에 해당하는 기록을 저장 또는 업데이트
    @Transactional
    public RecordResponse saveRecord(String email, LocalDate date, SlotType slotType, RecordRequest request) {
        log.info("기록 저장 요청 - 이메일: {}, 날짜: {}, 슬롯 타입: {}", email, date, slotType);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기존 기록 조회
        Record record = recordRepository.findByUserAndRecordDateAndSlotType(user, date, slotType)
                .orElse(null);

        // 기존 기록이 없으면 새로 생성, 있으면 업데이트
        if (record == null) {
            record = new Record();
            record.setUser(user);
            record.setRecordDate(date);
            record.setSlotType(slotType);
            log.info("새로운 기록 생성");
        } else {
            log.info("기존 기록 업데이트 - 기록 ID: {}", record.getId());
        }

        // 기록 내용 및 완료 상태 설정
        record.setContent(request.getContent());
        if (request.getCompleted()) {
            record.markAsCompleted();
        } else {
            record.markAsUncompleted();
        }

        // 기록 저장
        Record savedRecord = recordRepository.save(record);
        log.info("기록 저장 성공 - 기록 ID: {}", savedRecord.getId());

        return RecordResponse.from(savedRecord);
    }

    // 특정 사용자의 특정 날짜에 해당하는 모든 기록 조회
    @Transactional(readOnly = true)
    public DailyRecordsResponse getDailyRecords(String email, LocalDate date) {
        log.info("일일 기록 조회 요청 - 이메일: {}, 날짜: {}", email, date);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 해당 날짜의 모든 기록 조회
        List<Record> records = recordRepository.findByUserAndRecordDate(user, date);
        log.info("조회된 기록 수: {}", records.size());

        // Record 엔티티를 RecordResponse DTO로 변환
        List<RecordResponse> recordResponses = records.stream()
                .map(RecordResponse::from)
                .collect(Collectors.toList());

        // DailyRecordsResponse 생성 및 반환
        return DailyRecordsResponse.from(date, recordResponses);
    }

    // 특정 사용자의 특정 기간(주간)에 해당하는 모든 기록 조회
    @Transactional(readOnly = true)
    public List<RecordResponse> getWeeklyRecords(String email, LocalDate startDate, LocalDate endDate) {
        log.info("주간 기록 조회 요청 - 이메일: {}, 시작 날짜: {}, 종료 날짜: {}", email, startDate, endDate);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 해당 기간의 모든 기록 조회
        List<Record> records = recordRepository.findByUserAndRecordDateBetween(user, startDate, endDate);

        // 기록을 DTO로 변환
        return records.stream()
                .map(RecordResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 사용자의 특정 기록을 삭제
    @Transactional
    public void deleteRecord(String email, Long recordId) {
        log.info("기록 삭제 요청 - 이메일: {}, 기록 ID: {}", email, recordId);

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기록 조회
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("기록을 찾을 수 없습니다."));

        // 기록이 해당 사용자에 속하는지 확인
        if (!record.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 사용자의 기록이 아닙니다.");
        }

        // 기록 삭제
        recordRepository.delete(record);
        log.info("기록 삭제 성공 - 기록 ID: {}", recordId);
    }
}
