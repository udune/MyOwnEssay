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
public class EssayWithLikesResponse {
    private Long id;
    private String title;
    private String theme;
    private String authorNickname;
    private String shareSlug;
    private LocalDateTime publishedAt;
    private LocalDateTime likedAt;

    public static EssayWithLikesResponse from(Essay essay, LocalDateTime likedAt) {
        return EssayWithLikesResponse.builder()
                .id(essay.getId())
                .title(essay.getTitle())
                .theme(essay.getTheme() != null ? essay.getTheme().name() : null)
                .authorNickname(essay.getUser().getNickname())
                .shareSlug(essay.getShareSlug())
                .publishedAt(essay.getPublishedAt())
                .likedAt(likedAt)
                .build();
    }
}