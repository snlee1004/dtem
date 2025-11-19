package com.example.dtem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewWithUserDTO {
    
    // 리뷰 정보
    private Long reviewId;
    private Long tradeId;
    private String reviewerId;
    private String revieweeId;
    private Integer responseSpeed;
    private Integer rating;
    private Integer mannerScore;
    private String willTradeAgain;
    private String reviewContent;
    private LocalDateTime createdAt;
    
    // 리뷰 작성자 정보
    private String reviewerName;
    private String reviewerEmail;
    private String reviewerProfileImage;
}


