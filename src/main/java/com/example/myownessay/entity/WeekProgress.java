package com.example.myownessay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 주간 진행도 엔티티
 * 사용자의 주간 기록 완료 현황을 추적합니다.
 */
@Entity
@Table(name = "week_progress", uniqueConstraints = @UniqueConstraint(
        name = "unique_user_week",
        columnNames = {"user_id", "week_start"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 주의 시작 날짜 (월요일)
     */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    /**
     * 완료된 일수 (0-7)
     * 하루에 하나 이상의 슬롯을 완료하면 1일로 계산
     */
    @Builder.Default
    @Column(name = "completed_days", nullable = false)
    private Integer completedDays = 0;

    /**
     * 에세이 생성 여부
     */
    @Builder.Default
    @Column(name = "essay_generated", nullable = false)
    private Boolean essayGenerated = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 완료율 계산 (0.0 ~ 1.0)
     * @return 완료율
     */
    public double getCompletionRate() {
        return completedDays / 7.0;
    }

    /**
     * 에세이 생성 가능 여부 확인
     * 최소 3일의 기록이 완료되어야 에세이 생성 가능
     * @return 에세이 생성 가능 여부
     */
    public boolean canGenerateEssay() {
        return !essayGenerated && completedDays >= 3;
    }

    /**
     * 에세이 생성 완료 표시
     */
    public void markEssayGenerated() {
        this.essayGenerated = true;
    }

    /**
     * 완료된 일수 업데이트
     * @param completedDays 완료된 일수 (0-7)
     */
    public void updateCompletedDays(Integer completedDays) {
        if (completedDays < 0 || completedDays > 7) {
            throw new IllegalArgumentException("완료된 일수는 0에서 7 사이여야 합니다.");
        }
        this.completedDays = completedDays;
    }
}
