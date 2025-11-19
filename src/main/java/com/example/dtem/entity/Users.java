package com.example.dtem.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * 오늘득템 서비스를 이용하는 사용자 정보를 관리
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    
    /**
     * 회원 고유 ID (Primary Key)
     * Oracle Sequence를 사용하여 자동 생성
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "USER_SEQ", allocationSize = 1)
    @Column(name = "USERID")
    private Integer userId;
    /**
     * 이메일 (로그인 ID로 사용)
     * - 필수 입력 항목
     * - 중복 불가 (unique constraint)
     * - 최대 100자
     */
    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * 비밀번호 (암호화되어 저장)
     * - 필수 입력 항목
     * - 최대 255자 (암호화 후 길이 고려)
     */
    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;
    
    /**
     * 사용자 이름 (닉네임)
     * - 필수 입력 항목
     * - 서비스에서 표시되는 이름
     * - 최대 50자
     */
    @Column(name = "USERNAME", nullable = false, length = 50)
    private String username;
    
    /**
     * 전화번호
     * - 선택 입력 항목
     * - 거래 시 연락용
     * - 최대 20자
     */
    @Column(name = "PHONE", length = 20)
    private String phone;
    
    /**
     * 프로필 이미지 URL
     * - 선택 입력 항목
     * - 파일 서버 또는 CDN에 저장된 이미지 경로
     * - 최대 500자
     */
    @Column(name = "PROFILEIMAGE", length = 500)
    private String profileImage;
    
    /**
     * 자기소개
     * - 선택 입력 항목
     * - 마이페이지에 표시되는 소개글
     * - 최대 500자
     */
    @Column(name = "ADDR1", length = 500)
    private String addr1;
    /** 주소 1  */
    @Column(name = "ADDR2", length = 500)
    private String addr2;
    /** 주소 2  */
    @Column(name = "ADDR3", length = 500)
    private String addr3;
    /** 주소 3  */
    @Column(name = "ADDR4", length = 500)
    private String addr4;  
    /** 주소 4  */
    
    @Column(name = "INTRODUCE", length = 500)
    private String introduce;
    
    /**
     * 거래 평점 (별점)
     * - 다른 사용자들이 준 평가의 평균
     * - 0.0 ~ 5.0 범위
     * - 소수점 2자리까지 (예: 4.85)
     * - 기본값: 0.0
     */
    @Column(name = "RATING", columnDefinition = "NUMBER(3,2) DEFAULT 0.0")
    private Double rating;
    
    /**
     * 받은 리뷰 개수
     * - 거래 후 받은 평가의 총 개수
     * - 기본값: 0
     */
    @Column(name = "REVIEWCOUNT", columnDefinition = "NUMBER DEFAULT 0")
    private Integer reviewCount;
    
    /**
     * 판매 완료 횟수
     * - 상품을 성공적으로 판매한 횟수
     * - 기본값: 0
     */
    @Column(name = "SELLCOUNT", columnDefinition = "NUMBER DEFAULT 0")
    private Integer sellCount;
    
    /**
     * 구매 완료 횟수
     * - 상품을 성공적으로 구매한 횟수
     * - 기본값: 0
     */
    @Column(name = "BUYCOUNT", columnDefinition = "NUMBER DEFAULT 0")
    private Integer buyCount;
    /**
     * 회원가입 일시
     * - 자동으로 현재 시간이 설정됨
     * - 수정 불가
     */
    @CreationTimestamp
    @Column(name = "CREATEDAT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 회원정보 수정 일시
     * - 정보 수정 시 자동으로 현재 시간이 업데이트됨
     */
    @UpdateTimestamp
    @Column(name = "UPDATEDAT")
    private LocalDateTime updatedAt;
}


