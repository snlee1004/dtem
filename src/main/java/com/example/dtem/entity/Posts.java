package com.example.dtem.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 게시글(상품) 엔티티
 * 중고거래 상품 정보를 관리
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Posts {
    
    /**
     * 게시글 고유 ID (Primary Key)
     * Oracle Sequence를 사용하여 자동 생성
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_seq")
    @SequenceGenerator(name = "post_seq", sequenceName = "POST_SEQ", allocationSize = 1)
    @Column(name = "POSTID")
    private int postId;
    
    /**
     * 게시글 작성자 (판매자)
     * - User 엔티티와 N:1 관계
     * - LAZY 로딩: 필요할 때만 사용자 정보를 조회
     * - 필수 관계
     */
    @Column(name = "USERID", nullable = false)
    private int userId;
    
    /**
     * 상품명 (게시글 제목)
     * - 필수 입력 항목
     * - 최대 200자
     * - 예: "아이폰 14 Pro 256GB 팝니다"
     */
    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;
    
    /**
     * 상품 상세 설명
     * - 필수 입력 항목
     * - 최대 4000자
     * - 상품 상태, 구매 시기, 거래 방법 등 상세 정보 포함
     */
    @Column(name = "CONTENT", nullable = false, length = 4000)
    private String content;
    
    @Column(name = "POSTIMAGE", nullable = true, length = 500)
    private String postImage;
    
    /**
     * 판매 가격 (원)
     * - 필수 입력 항목
     * - 정수형 (Integer)
     * - 예: 850000 (85만원)
     */
    @Column(name = "PRICE", nullable = false)
    private int price;
    
    /**
     * 상품 카테고리
     * - 필수 입력 항목
     * - 최대 50자
     * - 예: "digital" (디지털/가전), "fashion" (의류), "furniture" (가구)
     */
    @Column(name = "CATEGORY", nullable = false, length = 50)
    private String category;
    
    /**
     * 거래 희망 지역
     * - 필수 입력 항목
     * - 최대 100자
     * - 예: "서울 강남구", "경기 성남시"
     */
    @Column(name = "LOCATION", nullable = false, length = 100)
    private String location;
    
    /**
     * 상품 상태
     * - NEW: 새상품 (미개봉, 미사용)
     * - LIKENEW: 거의 새것 (사용감 거의 없음)
     * - USED: 사용감 있음 (정상적인 사용 흔적)
     * - OLD: 많이 사용함 (사용 흔적 많음)
     */
    @Column(name = "CONDITIONSTATUS", length = 20)
    private String conditionStatus;
    
    /**
     * 거래 방법
     * - DIRECT: 직거래만 가능
     * - DELIVERY: 택배거래만 가능
     * - BOTH: 직거래, 택배거래 모두 가능
     */
    @Column(name = "TRADEMETHOD", length = 50)
    private String tradeMethod;
    
    /**
     * 가격 협상 가능 여부
     * - Y: 네고 가능 (가격 제안 받음)
     * - N: 네고 불가 (정가 판매)
     * - 기본값: N
     */
    @Column(name = "NEGOTIABLE", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String negotiable;
    
    /**
     * 게시글 판매 상태
     * - SELLING: 판매중 (구매 가능)
     * - RESERVED: 예약중 (구매자와 거래 진행 중)
     * - SOLD: 판매완료 (거래 완료됨)
     * - 기본값: SELLING
     */
    @Column(name = "STATUS", length = 20, columnDefinition = "VARCHAR2(20) DEFAULT 'SELLING'")
    private String status;
    
    /**
     * 조회수
     * - 게시글이 조회된 횟수
     * - 기본값: 0
     * - 같은 사용자가 여러 번 조회해도 카운트
     */
    @Column(name = "HIT", columnDefinition = "NUMBER DEFAULT 0")
    private int hit;
    
    /**
     * 찜한 사람 수
     * - 게시글을 찜한 사용자의 수
     * - 기본값: 0
     */
    @Column(name = "WISHLISTCOUNT", columnDefinition = "NUMBER DEFAULT 0")
    private int wishlistCount;
    
    /**
     * 게시글 작성 일시
     * - 자동으로 현재 시간이 설정됨
     * - 수정 불가
     */
    @CreationTimestamp
    @Column(name = "CREATEDAT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 게시글 수정 일시
     * - 정보 수정 시 자동으로 현재 시간이 업데이트됨
     */
    @UpdateTimestamp
    @Column(name = "UPDATEDAT")
    private LocalDateTime updatedAt;
}


