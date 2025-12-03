package com.example.myownessay.entity;

import com.example.myownessay.entity.enums.EssayTheme;
import com.example.myownessay.entity.enums.PublishStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "essays")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Essay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String aiDraft;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String finalContent;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EssayTheme theme;

    @Column(length = 500)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false, length = 20)
    @Builder.Default
    private PublishStatus publishStatus = PublishStatus.PRIVATE;

    @Column(unique = true, length = 100)
    private String shareSlug;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 에세이 발행 (공개 또는 공유)
     */
    public void publish(PublishStatus status) {
        if (status == PublishStatus.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 상태로는 발행할 수 없습니다.");
        }
        this.publishStatus = status;
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
        if (this.shareSlug == null) {
            this.shareSlug = generateShareSlug();
        }
    }

    /**
     * 에세이 비공개 처리
     */
    public void unpublish() {
        this.publishStatus = PublishStatus.PRIVATE;
        this.shareSlug = null;
    }

    /**
     * 공유용 슬러그 생성
     */
    private String generateShareSlug() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 공개 여부 확인
     */
    public boolean isPublic() {
        return this.publishStatus == PublishStatus.PUBLIC;
    }

    /**
     * 공유 가능 여부 확인
     */
    public boolean isShared() {
        return this.publishStatus == PublishStatus.SHARED || this.publishStatus == PublishStatus.PUBLIC;
    }
}
