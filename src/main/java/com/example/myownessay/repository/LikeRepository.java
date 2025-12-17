package com.example.myownessay.repository;

import com.example.myownessay.entity.Essay;
import com.example.myownessay.entity.Like;
import com.example.myownessay.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * 사용자가 특정 에세이를 좋아요 했는지 확인
     */
    Optional<Like> findByUserAndEssay(User user, Essay essay);

    /**
     * 사용자가 특정 에세이를 좋아요 했는지 여부 확인
     */
    boolean existsByUserAndEssay(User user, Essay essay);

    /**
     * 특정 에세이의 좋아요 수
     */
    long countByEssay(Essay essay);

    /**
     * 특정 사용자의 좋아요 목록 (페이징)
     */
    Page<Like> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 특정 에세이의 좋아요 수 (쿼리 최적화)
     */
    @Query("SELECT COUNT(l) FROM Like l WHERE l.essay.id = :essayId")
    long countByEssayId(@Param("essayId") Long essayId);
}