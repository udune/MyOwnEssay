package com.example.myownessay.service;

import com.example.myownessay.dto.essay.request.EssayCreateRequest;
import com.example.myownessay.dto.essay.request.EssayPublishRequest;
import com.example.myownessay.dto.essay.request.EssayUpdateRequest;
import com.example.myownessay.dto.essay.response.EssayResponse;
import com.example.myownessay.entity.Essay;
import com.example.myownessay.entity.User;
import com.example.myownessay.entity.enums.PublishStatus;
import com.example.myownessay.repository.EssayRepository;
import com.example.myownessay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EssayService {

    private final EssayRepository essayRepository;
    private final UserRepository userRepository;

    /**
     * 에세이 생성
     */
    @Transactional
    public EssayResponse createEssay(String email, EssayCreateRequest request) {
        log.info("에세이 생성 요청 - 이메일: {}", email);

        User user = getUserByEmail(email);

        Essay essay = Essay.builder()
                .user(user)
                .title(request.getTitle())
                .finalContent(request.getFinalContent())
                .theme(request.getTheme())
                .coverImage(request.getCoverImage())
                .weekStart(request.getWeekStart())
                .weekEnd(request.getWeekEnd())
                .aiDraft(request.getAiDraft())
                .publishStatus(PublishStatus.PRIVATE)
                .build();

        Essay savedEssay = essayRepository.save(essay);
        log.info("에세이 생성 완료 - ID: {}", savedEssay.getId());

        return EssayResponse.from(savedEssay);
    }

    /**
     * 에세이 조회 (본인 것만)
     */
    @Transactional(readOnly = true)
    public EssayResponse getEssay(String email, Long essayId) {
        log.info("에세이 조회 요청 - 이메일: {}, 에세이 ID: {}", email, essayId);

        User user = getUserByEmail(email);

        Essay essay = essayRepository.findByIdAndUser(essayId, user)
                .orElseThrow(() -> new RuntimeException("에세이를 찾을 수 없습니다."));

        return EssayResponse.from(essay);
    }

    /**
     * 내 에세이 목록 조회
     */
    @Transactional(readOnly = true)
    public List<EssayResponse> getMyEssays(String email) {
        log.info("내 에세이 목록 조회 요청 - 이메일: {}", email);

        User user = getUserByEmail(email);

        List<Essay> essays = essayRepository.findByUser(user);
        log.info("조회된 에세이 수: {}", essays.size());

        return essays.stream()
                .map(EssayResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 에세이 수정
     */
    @Transactional
    public EssayResponse updateEssay(String email, Long essayId, EssayUpdateRequest request) {
        log.info("에세이 수정 요청 - 이메일: {}, 에세이 ID: {}", email, essayId);

        User user = getUserByEmail(email);

        Essay essay = essayRepository.findByIdAndUser(essayId, user)
                .orElseThrow(() -> new RuntimeException("에세이를 찾을 수 없습니다."));

        if (request.getTitle() != null) {
            essay.setTitle(request.getTitle());
        }
        if (request.getFinalContent() != null) {
            essay.setFinalContent(request.getFinalContent());
        }
        if (request.getTheme() != null) {
            essay.setTheme(request.getTheme());
        }
        if (request.getCoverImage() != null) {
            essay.setCoverImage(request.getCoverImage());
        }

        Essay updatedEssay = essayRepository.save(essay);
        log.info("에세이 수정 완료 - ID: {}", updatedEssay.getId());

        return EssayResponse.from(updatedEssay);
    }

    /**
     * 에세이 발행/공개 상태 변경
     */
    @Transactional
    public EssayResponse publishEssay(String email, Long essayId, EssayPublishRequest request) {
        log.info("에세이 발행 요청 - 이메일: {}, 에세이 ID: {}, 상태: {}", email, essayId, request.getStatus());

        User user = getUserByEmail(email);

        Essay essay = essayRepository.findByIdAndUser(essayId, user)
                .orElseThrow(() -> new RuntimeException("에세이를 찾을 수 없습니다."));

        if (request.getStatus() == PublishStatus.PRIVATE) {
            essay.unpublish();
            log.info("에세이 비공개 처리 완료 - ID: {}", essayId);
        } else {
            essay.publish(request.getStatus());
            log.info("에세이 발행 완료 - ID: {}, 상태: {}, 슬러그: {}", essayId, request.getStatus(), essay.getShareSlug());
        }

        Essay publishedEssay = essayRepository.save(essay);
        return EssayResponse.from(publishedEssay);
    }

    /**
     * 에세이 삭제
     */
    @Transactional
    public void deleteEssay(String email, Long essayId) {
        log.info("에세이 삭제 요청 - 이메일: {}, 에세이 ID: {}", email, essayId);

        User user = getUserByEmail(email);
        Essay essay = essayRepository.findByIdAndUser(essayId, user)
                .orElseThrow(() -> new RuntimeException("에세이를 찾을 수 없습니다."));

        essayRepository.delete(essay);
        log.info("에세이 삭제 완료 - ID: {}", essayId);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}
