package com.example.myownessay.service;

import com.example.myownessay.dto.record.request.RecordRequest;
import com.example.myownessay.dto.record.response.DailyRecordsResponse;
import com.example.myownessay.dto.record.response.RecordResponse;
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
@DisplayName("기록 서비스 단위 테스트")
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecordService recordService;

    private User testUser;
    private LocalDate testDate;
    private Map<String, Object> testContent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNickname("테스터");

        testDate = LocalDate.of(2024, 6, 15);

        testContent = new HashMap<>();
        testContent.put("quote", "테스트 명언");
        testContent.put("author", "테스트 저자");
        testContent.put("thought", "테스트 생각");
    }

    @Test
    @DisplayName("기록 저장 - 새로운 기록 생성 성공")
    void saveRecord_새로운기록_성공() {
        // Given
        RecordRequest request = new RecordRequest();
        request.setContent(testContent);
        request.setCompleted(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDateAndSlotType(testUser, testDate, SlotType.READING))
                .thenReturn(Optional.empty());

        Record savedRecord = new Record();
        savedRecord.setId(1L);
        savedRecord.setUser(testUser);
        savedRecord.setRecordDate(testDate);
        savedRecord.setSlotType(SlotType.READING);
        savedRecord.setContent(testContent);
        savedRecord.markAsCompleted();

        when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);

        // When
        RecordResponse result = recordService.saveRecord("test@example.com", testDate, SlotType.READING, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(SlotType.READING, result.getSlotType());
        assertTrue(result.getIsCompleted());
        assertEquals(testContent, result.getContent());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(recordRepository, times(1)).findByUserAndRecordDateAndSlotType(testUser, testDate, SlotType.READING);
        verify(recordRepository, times(1)).save(any(Record.class));
    }

    @Test
    @DisplayName("기록 저장 - 기존 기록 업데이트 성공")
    void saveRecord_기존기록업데이트_성공() {
        // Given
        Record existingRecord = new Record();
        existingRecord.setId(1L);
        existingRecord.setUser(testUser);
        existingRecord.setRecordDate(testDate);
        existingRecord.setSlotType(SlotType.READING);
        existingRecord.setContent(new HashMap<>());
        existingRecord.markAsUncompleted();

        RecordRequest request = new RecordRequest();
        request.setContent(testContent);
        request.setCompleted(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDateAndSlotType(testUser, testDate, SlotType.READING))
                .thenReturn(Optional.of(existingRecord));
        when(recordRepository.save(any(Record.class))).thenReturn(existingRecord);

        // When
        RecordResponse result = recordService.saveRecord("test@example.com", testDate, SlotType.READING, request);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsCompleted());
        assertEquals(testContent, result.getContent());

        verify(recordRepository, times(1)).save(existingRecord);
    }

    @Test
    @DisplayName("기록 저장 - 사용자 없음 실패")
    void saveRecord_사용자없음_실패() {
        // Given
        RecordRequest request = new RecordRequest();
        request.setContent(testContent);
        request.setCompleted(true);

        when(userRepository.findByEmail("notexist@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recordService.saveRecord("notexist@example.com", testDate, SlotType.READING, request);
        });

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    @DisplayName("일일 기록 조회 - 성공")
    void getDailyRecords_성공() {
        // Given
        List<Record> records = new ArrayList<>();

        Record record1 = new Record();
        record1.setId(1L);
        record1.setUser(testUser);
        record1.setRecordDate(testDate);
        record1.setSlotType(SlotType.READING);
        record1.setContent(testContent);
        record1.markAsCompleted();
        records.add(record1);

        Record record2 = new Record();
        record2.setId(2L);
        record2.setUser(testUser);
        record2.setRecordDate(testDate);
        record2.setSlotType(SlotType.DIARY);
        record2.setContent(new HashMap<>());
        record2.markAsCompleted();
        records.add(record2);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDate(testUser, testDate)).thenReturn(records);

        // When
        DailyRecordsResponse result = recordService.getDailyRecords("test@example.com", testDate);

        // Then
        assertNotNull(result);
        assertEquals(testDate, result.getDate());
        assertEquals(2, result.getRecords().size());
        assertEquals(0.5, result.getCompletionRate()); // 2/4 = 0.5
        assertEquals(2, result.getCompletedCount());
        assertEquals(4, result.getTotalSlots());
        assertFalse(result.getIsAllCompleted());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(recordRepository, times(1)).findByUserAndRecordDate(testUser, testDate);
    }

    @Test
    @DisplayName("일일 기록 조회 - 모든 슬롯 완료")
    void getDailyRecords_모든슬롯완료() {
        // Given
        List<Record> records = new ArrayList<>();

        for (SlotType slotType : SlotType.values()) {
            Record record = new Record();
            record.setId(Long.valueOf(slotType.ordinal() + 1));
            record.setUser(testUser);
            record.setRecordDate(testDate);
            record.setSlotType(slotType);
            record.setContent(new HashMap<>());
            record.markAsCompleted();
            records.add(record);
        }

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDate(testUser, testDate)).thenReturn(records);

        // When
        DailyRecordsResponse result = recordService.getDailyRecords("test@example.com", testDate);

        // Then
        assertEquals(1.0, result.getCompletionRate()); // 4/4 = 1.0
        assertEquals(4, result.getCompletedCount());
        assertTrue(result.getIsAllCompleted());
        assertTrue(result.getIncompleteSlots().isEmpty());
    }

    @Test
    @DisplayName("주간 기록 조회 - 성공")
    void getWeeklyRecords_성공() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 6, 10);
        LocalDate endDate = LocalDate.of(2024, 6, 16);

        List<Record> weeklyRecords = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Record record = new Record();
            record.setId((long) (i + 1));
            record.setUser(testUser);
            record.setRecordDate(startDate.plusDays(i));
            record.setSlotType(SlotType.READING);
            record.setContent(testContent);
            record.markAsCompleted();
            weeklyRecords.add(record);
        }

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDateBetween(testUser, startDate, endDate))
                .thenReturn(weeklyRecords);

        // When
        List<RecordResponse> result = recordService.getWeeklyRecords("test@example.com", startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(7, result.size());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(recordRepository, times(1)).findByUserAndRecordDateBetween(testUser, startDate, endDate);
    }

    @Test
    @DisplayName("기록 삭제 - 성공")
    void deleteRecord_성공() {
        // Given
        Record record = new Record();
        record.setId(1L);
        record.setUser(testUser);
        record.setRecordDate(testDate);
        record.setSlotType(SlotType.READING);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(record));
        when(recordRepository.save(any(Record.class))).thenReturn(record);

        // When
        recordService.deleteRecord("test@example.com", 1L);

        // Then
        assertTrue(record.getIsDeleted());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(recordRepository, times(1)).findByIdAndUser(1L, testUser);
        verify(recordRepository, times(1)).save(record);
    }

    @Test
    @DisplayName("기록 삭제 - 기록 없음 실패")
    void deleteRecord_기록없음_실패() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recordService.deleteRecord("test@example.com", 999L);
        });

        assertEquals("기록을 찾을 수 없습니다.", exception.getMessage());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    @DisplayName("기록 삭제 - 다른 사용자의 기록 삭제 실패")
    void deleteRecord_다른사용자기록_실패() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recordService.deleteRecord("test@example.com", 1L);
        });

        assertEquals("기록을 찾을 수 없습니다.", exception.getMessage());
        verify(recordRepository, never()).save(any(Record.class));
    }

    @Test
    @DisplayName("기록 저장 - 미완료 상태로 저장")
    void saveRecord_미완료상태_성공() {
        // Given
        RecordRequest request = new RecordRequest();
        request.setContent(testContent);
        request.setCompleted(false);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(recordRepository.findByUserAndRecordDateAndSlotType(testUser, testDate, SlotType.READING))
                .thenReturn(Optional.empty());

        Record savedRecord = new Record();
        savedRecord.setId(1L);
        savedRecord.setUser(testUser);
        savedRecord.setRecordDate(testDate);
        savedRecord.setSlotType(SlotType.READING);
        savedRecord.setContent(testContent);
        savedRecord.markAsUncompleted();

        when(recordRepository.save(any(Record.class))).thenReturn(savedRecord);

        // When
        RecordResponse result = recordService.saveRecord("test@example.com", testDate, SlotType.READING, request);

        // Then
        assertFalse(result.getIsCompleted());
    }
}