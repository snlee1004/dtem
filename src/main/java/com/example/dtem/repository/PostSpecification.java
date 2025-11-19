package com.example.dtem.repository;

import org.springframework.data.jpa.domain.Specification;
import com.example.dtem.entity.Posts;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시물 동적 검색을 위한 Specification 클래스
 */
public class PostSpecification {

    /**
     * 동적 검색 조건 생성
     * 
     * @param title 제목 검색어 (부분 일치)
     * @param sido 시/도
     * @param sigungu 시/군/구
     * @param dong 읍/면/동
     * @param category 카테고리
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @return Specification<Posts>
     */
    public static Specification<Posts> searchPosts(
            String title,
            String sido,
            String sigungu,
            String dong,
            String category,
            Integer minPrice,
            Integer maxPrice) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 0. SOLD 상품 제외 (거래완료된 상품은 목록에서 숨김)
            predicates.add(criteriaBuilder.notEqual(root.get("status"), "SOLD"));

            // 1. 제목 검색 (부분 일치, 대소문자 무시)
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
                ));
            }

            // 2. 위치 검색 (계층적 검색 - ~로 시작)
            if (dong != null && !dong.trim().isEmpty()) {
                // 동까지 선택한 경우: "시/도 시/군/구 동"으로 시작
                if (sido != null && !sido.isEmpty() && sigungu != null && !sigungu.isEmpty()) {
                    String fullLocation = sido + " " + sigungu + " " + dong;
                    predicates.add(criteriaBuilder.like(root.get("location"), fullLocation + "%"));
                }
            } else if (sigungu != null && !sigungu.trim().isEmpty()) {
                // 시/군/구까지만 선택한 경우: "시/도 시/군/구"로 시작
                if (sido != null && !sido.isEmpty()) {
                    String partialLocation = sido + " " + sigungu;
                    predicates.add(criteriaBuilder.like(root.get("location"), partialLocation + "%"));
                }
            } else if (sido != null && !sido.trim().isEmpty()) {
                // 시/도만 선택한 경우: "시/도"로 시작
                predicates.add(criteriaBuilder.like(root.get("location"), sido + "%"));
            }

            // 3. 카테고리 검색 (전체보기가 아닌 경우만)
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("category"), "%" + category + "%"));
            }

            // 4. 가격 검색
            if (minPrice != null && maxPrice != null) {
                if (minPrice.equals(0) && maxPrice.equals(0)) {
                    // 나눔 (0원)
                    predicates.add(criteriaBuilder.equal(root.get("price"), 0));
                } else {
                    // 최소가격 ~ 최대가격
                    predicates.add(criteriaBuilder.between(root.get("price"), minPrice, maxPrice));
                }
            } else if (minPrice != null) {
                // 최소가격만 지정
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            } else if (maxPrice != null) {
                // 최대가격만 지정
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 모든 조건을 AND로 결합
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

