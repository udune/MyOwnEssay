package com.example.myownessay.dto.essay.response;

import com.example.myownessay.entity.Essay;
import com.example.myownessay.entity.enums.EssayTheme;
import com.example.myownessay.entity.enums.PublishStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EssayResponse {

    private Long id;

    private String title;

    private String aiDraft;

    private String finalContent;

    private EssayTheme theme;

    private String coverImage;

    private PublishStatus publishStatus;

    private String shareSlug;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    /**
     * Entity -> DTO 변환 메서드
     */
    public static EssayResponse from(Essay essay) {
        return EssayResponse.builder()
                .id(essay.getId())
                .title(essay.getTitle())
                .aiDraft(essay.getAiDraft())
                .finalContent(essay.getFinalContent())
                .theme(essay.getTheme())
                .coverImage(essay.getCoverImage())
                .publishStatus(essay.getPublishStatus())
                .shareSlug(essay.getShareSlug())
                .weekStart(essay.getWeekStart())
                .weekEnd(essay.getWeekEnd())
                .createdAt(essay.getCreatedAt())
                .updatedAt(essay.getUpdatedAt())
                .publishedAt(essay.getPublishedAt())
                .build();
    }

    /**
     * 공개 여부 확인
     */
    public boolean isPublic() {
        return publishStatus == PublishStatus.PUBLIC;
    }

    /**
     * 공유 가능 여부 확인
     */
    public boolean isShared() {
        return publishStatus == PublishStatus.SHARED || publishStatus == PublishStatus.PUBLIC;
    }
}
