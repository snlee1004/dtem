package com.example.dtem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"review\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_seq")
    @SequenceGenerator(name = "review_seq", sequenceName = "review_seq", allocationSize = 1)
    @Column(name = "\"review_id\"")
    private Long reviewId;
    
    @Column(name = "\"trade_id\"", nullable = false)
    private Long tradeId;  // 거래 ID
    
    @Column(name = "\"reviewer_id\"", nullable = false)
    private String reviewerId;  // 리뷰 작성자 ID
    
    @Column(name = "\"reviewee_id\"", nullable = false)
    private String revieweeId;  // 리뷰 받는 사람 ID
    
    @Column(name = "\"response_speed\"")
    private Integer responseSpeed;  // 응답속도 (1-5)
    
    @Column(name = "\"rating\"", nullable = false)
    private Integer rating;  // 별점 (1-5)
    
    @Column(name = "\"manner_score\"")
    private Integer mannerScore;  // 매너지수 (1-5)
    
    @Column(name = "\"will_trade_again\"", length = 1)
    private String willTradeAgain;  // 재거래희망 (Y/N)
    
    @Column(name = "\"review_content\"", length = 2000)
    private String reviewContent;  // 리뷰 내용
    
    @Column(name = "\"created_at\"", nullable = false)
    private LocalDateTime createdAt;  // 작성일
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

