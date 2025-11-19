package com.example.dtem.service;

import com.example.dtem.dto.ReviewWithUserDTO;
import com.example.dtem.entity.Review;
import com.example.dtem.entity.Users;
import com.example.dtem.repository.ReviewRepository;
import com.example.dtem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    
    // 리뷰 저장
    @Transactional
    public Review saveReview(Review review) {
        log.info("💾 리뷰 저장 - tradeId: {}, reviewerId: {}, revieweeId: {}", 
                 review.getTradeId(), review.getReviewerId(), review.getRevieweeId());
        
        // 이미 작성한 리뷰가 있는지 확인 (tradeId + reviewerId 조합으로 체크)
        reviewRepository.findByTradeIdAndReviewerId(review.getTradeId(), review.getReviewerId())
            .ifPresent(existingReview -> {
                throw new RuntimeException("이미 리뷰를 작성했습니다.");
            });
        
        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        
        log.info("✅ 리뷰 저장 완료 - reviewId: {}", savedReview.getReviewId());
        return savedReview;
    }
    
    // 특정 사용자가 받은 리뷰 목록
    @Transactional(readOnly = true)
    public List<Review> getReceivedReviews(String userId) {
        return reviewRepository.findByRevieweeId(userId);
    }
    
    // 특정 사용자가 작성한 리뷰 목록
    @Transactional(readOnly = true)
    public List<Review> getWrittenReviews(String userId) {
        return reviewRepository.findByReviewerId(userId);
    }
    
    // 평균 별점 계산
    @Transactional(readOnly = true)
    public Double getAverageRating(String userId) {
        Double avg = reviewRepository.getAverageRatingByRevieweeId(userId);
        return avg != null ? Math.round(avg * 10) / 10.0 : 0.0;
    }
    
    // 리뷰 개수
    @Transactional(readOnly = true)
    public Long getReviewCount(String userId) {
        return reviewRepository.countByRevieweeId(userId);
    }
    
    // 받은 리뷰 목록 (작성자 정보 포함)
    @Transactional(readOnly = true)
    public List<ReviewWithUserDTO> getReceivedReviewsWithUser(String userId) {
        List<Review> reviews = reviewRepository.findByRevieweeId(userId);
        
        return reviews.stream().map(review -> {
            // 리뷰 작성자 정보 조회
            Users reviewer = null;
            try {
                Integer reviewerId = Integer.parseInt(review.getReviewerId());
                reviewer = userRepository.findById(reviewerId).orElse(null);
            } catch (NumberFormatException e) {
                log.warn("⚠️ 리뷰어 ID 파싱 실패: {}", review.getReviewerId());
            }
            
            return ReviewWithUserDTO.builder()
                    .reviewId(review.getReviewId())
                    .tradeId(review.getTradeId())
                    .reviewerId(review.getReviewerId())
                    .revieweeId(review.getRevieweeId())
                    .responseSpeed(review.getResponseSpeed())
                    .rating(review.getRating())
                    .mannerScore(review.getMannerScore())
                    .willTradeAgain(review.getWillTradeAgain())
                    .reviewContent(review.getReviewContent())
                    .createdAt(review.getCreatedAt())
                    .reviewerName(reviewer != null ? reviewer.getUsername() : "알 수 없음")
                    .reviewerEmail(reviewer != null ? reviewer.getEmail() : "")
                    .reviewerProfileImage(reviewer != null ? reviewer.getProfileImage() : null)
                    .build();
        }).collect(Collectors.toList());
    }
}

