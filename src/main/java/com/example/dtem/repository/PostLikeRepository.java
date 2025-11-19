package com.example.dtem.repository;

import com.example.dtem.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {
    
    // 특정 사용자가 특정 게시물을 찜했는지 확인
    Optional<PostLike> findByPostIdAndUserId(Integer postId, Integer userId);
    
    // 특정 사용자가 특정 게시물을 찜했는지 여부
    boolean existsByPostIdAndUserId(Integer postId, Integer userId);
    
    // 특정 게시물의 찜 개수
    long countByPostId(Integer postId);
    
    // 특정 사용자의 찜 목록 조회 (페이징) - Posts 정보 JOIN
    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.post WHERE pl.userId = :userId ORDER BY pl.createdAt DESC")
    Page<PostLike> findByUserIdWithPost(@Param("userId") Integer userId, Pageable pageable);
    
    // 특정 사용자의 찜 개수
    long countByUserId(Integer userId);
}

