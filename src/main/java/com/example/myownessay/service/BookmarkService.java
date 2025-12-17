package com.example.myownessay.service;

import com.example.myownessay.dto.essay.response.BookmarkResponse;
import com.example.myownessay.dto.essay.response.EssayWithBookmarkResponse;
import com.example.myownessay.entity.Bookmark;
import com.example.myownessay.entity.Essay;
import com.example.myownessay.entity.User;
import com.example.myownessay.repository.BookmarkRepository;
import com.example.myownessay.repository.EssayRepository;
import com.example.myownessay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final EssayRepository essayRepository;

    /**
     * 북마크 추가 (멱등성)
     */
    @Transactional
    public BookmarkResponse addBookmark(String email, Long essayId) {
        log.info("북마크 추가 요청 - 이메일: {}, 에세이 ID: {}", email, essayId);

        User user = getUserByEmail(email);
        Essay essay = getEssayById(essayId);

        // 이미 북마크를 했는지 확인 (멱등성)
        if (!bookmarkRepository.existsByUserAndEssay(user, essay)) {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .essay(essay)
                    .build();
            bookmarkRepository.save(bookmark);
            log.info("북마크 추가 완료 - 사용자: {}, 에세이: {}", user.getId(), essayId);
        } else {
            log.info("이미 북마크한 에세이입니다 - 사용자: {}, 에세이: {}", user.getId(), essayId);
        }

        long bookmarkCount = bookmarkRepository.countByEssayId(essayId);
        return BookmarkResponse.builder()
                .bookmarked(true)
                .bookmarkCount(bookmarkCount)
                .build();
    }

    /**
     * 북마크 취소
     */
    @Transactional
    public BookmarkResponse removeBookmark(String email, Long essayId) {
        log.info("북마크 취소 요청 - 이메일: {}, 에세이 ID: {}", email, essayId);

        User user = getUserByEmail(email);
        Essay essay = getEssayById(essayId);

        bookmarkRepository.findByUserAndEssay(user, essay).ifPresent(bookmark -> {
            bookmarkRepository.delete(bookmark);
            log.info("북마크 취소 완료 - 사용자: {}, 에세이: {}", user.getId(), essayId);
        });

        long bookmarkCount = bookmarkRepository.countByEssayId(essayId);
        return BookmarkResponse.builder()
                .bookmarked(false)
                .bookmarkCount(bookmarkCount)
                .build();
    }

    /**
     * 내 북마크 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<EssayWithBookmarkResponse> getMyBookmarks(String email, Pageable pageable) {
        log.info("내 북마크 목록 조회 요청 - 이메일: {}", email);

        User user = getUserByEmail(email);
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return bookmarks.map(bookmark -> EssayWithBookmarkResponse.from(bookmark.getEssay(), bookmark.getCreatedAt()));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    private Essay getEssayById(Long essayId) {
        return essayRepository.findById(essayId)
                .orElseThrow(() -> new RuntimeException("에세이를 찾을 수 없습니다."));
    }
}