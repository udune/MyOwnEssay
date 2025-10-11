package com.example.myownessay.service;

import com.example.myownessay.entity.Record;
import com.example.myownessay.entity.User;
import com.example.myownessay.entity.enums.SlotType;
import com.example.myownessay.repository.RecordRepository;
import com.example.myownessay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("연속 기록 일수 서비스 테스트")
class StreakServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StreakService streakService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("테스터");
    }

    @Test
    @DisplayName("현재 연속 기록 일수 계산 - 3일 연속")
    void getCurrentStreak_3일연속() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LocalDate today = LocalDate.now();

        // 오늘, 어제, 그제 완료된 기록 생성
        when(recordRepository.findByUserAndRecordDate(testUser, today))
                .thenReturn(createCompletedRecords(today));
        when(recordRepository.findByUserAndRecordDate(testUser, today.minusDays(1)))
                .thenReturn(createCompletedRecords(today.minusDays(1)));
        when(recordRepository.findByUserAndRecordDate(testUser, today.minusDays(2)))
                .thenReturn(createCompletedRecords(today.minusDays(2)));
        when(recordRepository.findByUserAndRecordDate(testUser, today.minusDays(3)))
                .thenReturn(Collections.emptyList());

        // When
        int currentStreak = streakService.getCurrentStreak("test@example.com");

        // Then
        assertEquals(3, currentStreak);
    }

    @Test
    @DisplayName("현재 연속 기록 일수 계산 - 기록 없음")
    void getCurrentStreak_기록없음() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDate(any(), any())).thenReturn(Collections.emptyList());

        // When
        int currentStreak = streakService.getCurrentStreak("test@example.com");

        // Then
        assertEquals(0, currentStreak);
    }

    @Test
    @DisplayName("최대 연속 기록 일수 계산 - 중간에 끊김")
    void getMaxStreak_중간에끊김() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LocalDate today = LocalDate.now();
        List<Record> records = new ArrayList<>();

        // 5일 연속 (today ~ today-4)
        for (int i = 0; i < 5; i++) {
            records.add(createCompletedRecord(today.minusDays(i)));
        }

        // 1일 공백 (today-5)

        // 3일 연속 (today-6 ~ today-8)
        for (int i = 6; i < 9; i++) {
            records.add(createCompletedRecord(today.minusDays(i)));
        }

        when(recordRepository.findByUserAndRecordDateBetween(
                eq(testUser), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);

        // When
        int maxStreak = streakService.getMaxStreak("test@example.com");

        // Then
        assertEquals(5, maxStreak);
    }

    @Test
    @DisplayName("최대 연속 기록 일수 계산 - 기록 없음")
    void getMaxStreak_기록없음() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDateBetween(
                any(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        int maxStreak = streakService.getMaxStreak("test@example.com");

        // Then
        assertEquals(0, maxStreak);
    }

    private List<Record> createCompletedRecords(LocalDate date) {
        List<Record> records = new ArrayList<>();
        Record record = new Record();
        record.setRecordDate(date);
        record.markAsCompleted();
        records.add(record);
        return records;
    }

    private Record createCompletedRecord(LocalDate date) {
        Record record = new Record();
        record.setUser(testUser);
        record.setRecordDate(date);
        record.setSlotType(SlotType.READING);
        record.setContent(new HashMap<>());
        record.markAsCompleted();
        return record;
    }
}