package com.example.dtem.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.dtem.entity.Posts;

@Repository
public interface PostRepository extends JpaRepository<Posts, Integer>, JpaSpecificationExecutor<Posts> {

    // 카테고리별 게시글 조회
    List<Posts> findByCategory(String category);

    // 회원별 게시글 조회
    List<Posts> findByUserId(int userId);

    // 판매중 게시글 조회
    List<Posts> findByStatus(String status);

    // 제목 검색 (대소문자 구분 없이)
    List<Posts> findByTitleContainingIgnoreCase(String keyword);
    
    // 찜 많은 순서대로 상위 4개 조회 (SOLD 제외)
    @Query("SELECT p FROM Posts p WHERE p.status != 'SOLD' ORDER BY p.wishlistCount DESC")
    List<Posts> findTop4ByStatusNotOrderByWishlistCountDesc(Pageable pageable);
    
    // 최신 순서대로 상위 12개 조회 (SOLD 제외)
    @Query("SELECT p FROM Posts p WHERE p.status != 'SOLD' ORDER BY p.createdAt DESC")
    List<Posts> findTop12ByStatusNotOrderByCreatedAtDesc(Pageable pageable);
    
    // 특정 사용자의 특정 상태 게시글 조회 (페이징)
    Page<Posts> findByUserIdAndStatus(Integer userId, String status, Pageable pageable);
}


