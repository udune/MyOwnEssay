package com.example.myownessay.dto.essay.response;

import com.example.myownessay.entity.Essay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayWithBookmarkResponse {
    private Long id;
    private String title;
    private String theme;
    private String authorNickname;
    private String shareSlug;
    private LocalDateTime publishedAt;
    private LocalDateTime bookmarkedAt;

    public static EssayWithBookmarkResponse from(Essay essay, LocalDateTime bookmarkedAt) {
        return EssayWithBookmarkResponse.builder()
                .id(essay.getId())
                .title(essay.getTitle())
                .theme(essay.getTheme() != null ? essay.getTheme().name() : null)
                .authorNickname(essay.getUser().getNickname())
                .shareSlug(essay.getShareSlug())
                .publishedAt(essay.getPublishedAt())
                .bookmarkedAt(bookmarkedAt)
                .build();
    }
}