package com.example.myownessay.entity;

import com.example.myownessay.entity.enums.SlotType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "records", uniqueConstraints = @UniqueConstraint(
        name = "unique_record",
        columnNames = {"user_id", "record_date", "slot_type"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false, length = 20)
    private SlotType slotType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> content;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 기록 완료 상태 설정 메서드
    public void markAsCompleted() {
        this.isCompleted = true;
    }

    // 기록 미완료 상태 설정 메서드
    public void markAsUncompleted() {
        this.isCompleted = false;
    }

    // content 맵에서 특정 키의 값을 가져오는 메서드
    public Object getContentValue(String key) {
        return content != null ? content.get(key) : null;
    }

    // content 맵에 특정 키-값 쌍을 추가하거나 업데이트하는 메서드
    public void setContentValue(String key, Object value) {
        if (this.content == null) {
            this.content = new java.util.HashMap<>();
        }

        this.content.put(key, value);
    }

    // 기록이 완료되었는지 여부를 반환하는 메서드
    public boolean isCompleted() {
        return Boolean.TRUE.equals(this.isCompleted);
    }
}
