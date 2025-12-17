package com.example.myownessay.service;

import com.example.myownessay.dto.essay.response.EssayWithLikesResponse;
import com.example.myownessay.dto.essay.response.LikeResponse;
import com.example.myownessay.entity.Essay;
import com.example.myownessay.entity.Like;
import com.example.myownessay.entity.User;
import com.example.myownessay.repository.EssayRepository;
import com.example.myownessay.repository.LikeRepository;
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
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final EssayRepository essayRepository;

    /**
     * 좋아요 추가 (멱등성)
     */
    @Transactional
    public LikeResponse addLike(String email, Long essayId) {
        log.info("좋아요 추가 요청 - 이메일: {}, 에세이 ID: {}", email, essayId);

        User user = getUserByEmail(email);
        Essay essay = getEssayById(essayId);

        // 이미 좋아요를 눌렀는지 확인 (멱등성)
        if (!likeRepository.existsByUserAndEssay(user, essay)) {
            Like like = Like.builder()
                    .user(user)
                    .essay(essay)
                    .build();
            likeRepository.save(like);
            log.info("좋아요 추가 완료 - 사용자: {}, 에세이: {}", user.getId(), essayId);
        } else {
            log.info("이미 좋아요를 누른 에세이입니다 - 사용자: {}, 에세이: {}", user.getId(), essayId);
        }

        long likeCount = likeRepository.countByEssayId(essayId);
        return LikeResponse.builder()
                .liked(true)
                .likeCount(likeCount)
                .build();
    }

    /**
     * 좋아요 취소
     */
    @Transactional
    public LikeResponse removeLike(String email, Long essayId) {
        log.info("좋아요 취소 요청 - 이메일: {}, 에세이 ID: {}", email, essayId);

        User user = getUserByEmail(email);
        Essay essay = getEssayById(essayId);

        likeRepository.findByUserAndEssay(user, essay).ifPresent(like -> {
            likeRepository.delete(like);
            log.info("좋아요 취소 완료 - 사용자: {}, 에세이: {}", user.getId(), essayId);
        });

        long likeCount = likeRepository.countByEssayId(essayId);
        return LikeResponse.builder()
                .liked(false)
                .likeCount(likeCount)
                .build();
    }

    /**
     * 내 좋아요 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<EssayWithLikesResponse> getMyLikes(String email, Pageable pageable) {
        log.info("내 좋아요 목록 조회 요청 - 이메일: {}", email);

        User user = getUserByEmail(email);
        Page<Like> likes = likeRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return likes.map(like -> EssayWithLikesResponse.from(like.getEssay(), like.getCreatedAt()));
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