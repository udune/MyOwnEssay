package com.example.myownessay.repository;

import com.example.myownessay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 조회
    Optional<User> findByNickname(String nickname);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname);

}
