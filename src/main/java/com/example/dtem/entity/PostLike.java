package com.example.dtem.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 게시물 찜(좋아요) 엔티티
 */
@Entity
@Table(name = "POST_LIKE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "like_seq")
    @SequenceGenerator(name = "like_seq", sequenceName = "POST_LIKE_LIKESEQ_SEQ", allocationSize = 1)
    @Column(name = "LIKESEQ")
    private Integer likeSeq;
    
    @Column(name = "POSTID", nullable = false)
    private Integer postId;
    
    @Column(name = "USERID", nullable = false)
    private Integer userId;
    
    @Column(name = "CREATEDAT")
    private LocalDateTime createdAt;
    
    // Posts와의 관계 (찜 목록 조회 시 상품 정보 JOIN용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSTID", insertable = false, updatable = false)
    private Posts post;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

