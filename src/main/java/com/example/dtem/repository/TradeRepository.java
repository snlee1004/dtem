package com.example.dtem.repository;

import com.example.dtem.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    // 상품과 구매자로 거래 찾기
    @Query("SELECT t FROM Trade t WHERE t.postId = :postId AND t.buyerId = :buyerId")
    Optional<Trade> findByPostIdAndBuyerId(@Param("postId") Long postId, 
                                           @Param("buyerId") String buyerId);
    
    // roomId로 거래 찾기
    Optional<Trade> findByRoomId(String roomId);
    
    // 상품ID로 거래 찾기 (단일)
    Optional<Trade> findByPostId(Long postId);
    
    // 상품ID로 모든 거래 찾기 (다중)
    @Query("SELECT t FROM Trade t WHERE t.postId = :postId")
    List<Trade> findAllByPostId(@Param("postId") Long postId);
    
    // 판매자로서 완료한 거래 횟수
    Long countBySellerIdAndStatus(String sellerId, Trade.TradeStatus status);
    
    // 구매자로서 완료한 거래 횟수
    Long countByBuyerIdAndStatus(String buyerId, Trade.TradeStatus status);
    
    // 구매자의 모든 거래 조회 (최신순)
    @Query("SELECT t FROM Trade t WHERE t.buyerId = :buyerId ORDER BY t.createdAt DESC")
    List<Trade> findByBuyerIdOrderByCreatedAtDesc(@Param("buyerId") String buyerId);
}


