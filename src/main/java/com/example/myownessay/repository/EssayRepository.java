package com.example.myownessay.repository;

import com.example.myownessay.entity.Essay;
import com.example.myownessay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EssayRepository extends JpaRepository<Essay, Long> {

    /**
     * 특정 사용자의 모든 에세이 조회
     */
    List<Essay> findByUser(User user);

    /**
     * 특정 사용자의 에세이 ID로 조회
     */
    Optional<Essay> findByIdAndUser(Long id, User user);

    /**
     * 공유 슬러그로 에세이 조회
     */
    Optional<Essay> findByShareSlug(String shareSlug);
}
