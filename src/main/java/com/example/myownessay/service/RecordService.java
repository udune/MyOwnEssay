package com.example.myownessay.service;

import com.example.myownessay.dto.record.request.RecordRequest;
import com.example.myownessay.dto.record.response.DailyRecordsResponse;
import com.example.myownessay.dto.record.response.RecordResponse;
import com.example.myownessay.entity.User;
import com.example.myownessay.entity.Record;
import com.example.myownessay.entity.enums.SlotType;
import com.example.myownessay.repository.RecordRepository;
import com.example.myownessay.repository.UserRepository;
import com.example.myownessay.validator.SlotContentValidator;
import com.example.myownessay.validator.SlotValidatorFactory;
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
    private final SlotValidatorFactory validatorFactory;
    private final RecordCompletionService recordCompletionService;

    // 특정 사용자의 특정 기록을 소프트 삭제
    @Transactional
    public void deleteRecord(String email, Long recordId) {
        log.info("기록 삭제 요청 (Soft Delete) - 이메일: {}, 기록 ID: {}", email, recordId);

        User user = getUserByEmail(email);

        // 기록 조회(소프트 삭제되지 않은 기록만 조회)
        Record record = recordRepository.findByIdAndUser(recordId, user)
                .orElseThrow(() -> new RuntimeException("기록을 찾을 수 없습니다."));

        // Soft Delete 처리
        record.markAsDeleted();
        recordRepository.save(record);

        log.info("기록 소프트 삭제 성공 - 기록 ID: {}", recordId);
    }

    // 특정 사용자의 특정 기록을 복원
    public void restoreRecord(String email, Long recordId) {
        log.info("기록 복원 요청 - 이메일: {}, 기록 ID: {}", email, recordId);

        User user = getUserByEmail(email);

        // 기록 조회(삭제된 기록 포함)
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("기록을 찾을 수 없습니다."));

        // 권한 확인
        if (!record.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("해당 기록에 대한 권한이 없습니다.");
        }

        // Soft Delete 해제 처리
        record.restore();
        recordRepository.save(record);

        log.info("기록 복원 성공 - 기록 ID: {}", recordId);
    }

    // 특정 사용자의 특정 날짜와 슬롯 타입에 해당하는 기록을 저장 또는 업데이트
    @Transactional
    public RecordResponse saveRecord(String email, LocalDate date, SlotType slotType, RecordRequest request) {
        log.info("기록 저장 요청 - 이메일: {}, 날짜: {}, 슬롯 타입: {}", email, date, slotType);

        // 슬롯 콘텐츠 유효성 검사
        SlotContentValidator validator = validatorFactory.getValidator(slotType);
        validator.validate(request.getContent());
        log.info("슬롯 콘텐츠 유효성 검사 통과");

        User user = getUserByEmail(email);

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

        User user = getUserByEmail(email);

        // 해당 날짜의 모든 기록 조회
        List<Record> records = recordRepository.findByUserAndRecordDate(user, date);
        log.info("조회된 기록 수: {}", records.size());

        // Record 엔티티를 RecordResponse DTO로 변환
        List<RecordResponse> recordResponses = records.stream()
                .map(RecordResponse::from)
                .collect(Collectors.toList());

        int completedCount = (int) records.stream()
                .filter(Record::getIsCompleted)
                .count();
        log.info("완료된 기록 수: {}", completedCount);

        double completionRate = recordCompletionService.calculateDailyCompletion(completedCount);
        boolean isAllCompleted = recordCompletionService.isAllCompleted(completedCount);
        log.info("계산된 완료율: {}, 모든 슬롯 완료 여부: {}", completionRate, isAllCompleted);

        // DailyRecordsResponse 생성 및 반환
        return DailyRecordsResponse.from(date, recordResponses, completionRate, completedCount, isAllCompleted);
    }

    // 특정 사용자의 특정 기간(주간)에 해당하는 모든 기록 조회
    @Transactional(readOnly = true)
    public List<RecordResponse> getWeeklyRecords(String email, LocalDate startDate, LocalDate endDate) {
        log.info("주간 기록 조회 요청 - 이메일: {}, 시작 날짜: {}, 종료 날짜: {}", email, startDate, endDate);

        // 날짜 유효성 검사
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료 날짜는 시작 날짜보다 이전일 수 없습니다.");
        }

        // 최대 31일 기간 제한
        if (startDate.plusDays(31).isBefore(endDate)) {
            throw new IllegalArgumentException("조회 기간은 최대 31일을 초과할 수 없습니다.");
        }

        User user = getUserByEmail(email);

        // 해당 기간의 모든 기록 조회
        List<Record> records = recordRepository.findByUserAndRecordDateBetween(user, startDate, endDate);

        // 기록을 DTO로 변환
        return records.stream()
                .map(RecordResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 사용자의 특정 주의 완료율 계산
    @Transactional(readOnly = true)
    public double calculateWeeklyCompletionRate(String email, LocalDate weekStart) {
        log.info("주간 완료율 계산 요청 - 이메일: {}, 주 시작 날짜: {}", email, weekStart);

        User user = getUserByEmail(email);

        // 주의 끝 날짜 계산
        LocalDate weekEnd = weekStart.plusDays(6);
        int completedDays = 0;

        // 주간 각 날짜별로 완료된 기록 수 확인
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            List<Record> dailyRecords = recordRepository.findByUserAndRecordDate(user, date);

            int completedCount = (int) dailyRecords.stream()
                    .filter(Record::getIsCompleted)
                    .count();

            if (recordCompletionService.isAllCompleted(completedCount)) {
                completedDays++;
            }
        }

        // 주간 완료율 계산
        double weeklyRate = recordCompletionService.calculateWeeklyCompletion(completedDays);

        log.info("계산된 주간 완료율: {}", weeklyRate);

        return weeklyRate;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}
