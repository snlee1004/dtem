package com.example.dtem.repository;

import com.example.dtem.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 특정 사용자가 받은 리뷰 목록 조회
    List<Review> findByRevieweeId(String revieweeId);
    
    // 특정 사용자가 작성한 리뷰 목록 조회
    List<Review> findByReviewerId(String reviewerId);
    
    // 특정 거래의 리뷰 조회
    Optional<Review> findByTradeId(Long tradeId);
    
    // 특정 거래에서 특정 사용자가 작성한 리뷰 조회 (한 거래당 2개 리뷰 가능)
    Optional<Review> findByTradeIdAndReviewerId(Long tradeId, String reviewerId);
    
    // 특정 사용자가 받은 리뷰 평균 별점 계산
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :revieweeId")
    Double getAverageRatingByRevieweeId(@Param("revieweeId") String revieweeId);
    
    // 특정 사용자가 받은 리뷰 개수
    Long countByRevieweeId(String revieweeId);
}

