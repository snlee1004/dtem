package com.example.dtem.service;

import com.example.dtem.entity.Posts;
import com.example.dtem.entity.Trade;
import com.example.dtem.repository.PostRepository;
import com.example.dtem.repository.TradeRepository;
import com.example.dtem.repository.ChatMessageRepository;
import com.example.dtem.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {
    
    private final TradeRepository tradeRepository;
    private final PostRepository postRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    
    // 거래 생성
    @Transactional
    public Trade createTrade(Long postId, String sellerId, String buyerId, String roomId) {
        Trade trade = Trade.builder()
                .postId(postId)
                .sellerId(sellerId)
                .buyerId(buyerId)
                .roomId(roomId)
                .status(Trade.TradeStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();
        
        return tradeRepository.save(trade);
    }
    
    // 거래 완료 처리
    @Transactional
    public Trade completeTrade(String roomId, Long postId) {
        log.info("💼 거래 완료 처리 시작 - roomId: {}, postId: {}", roomId, postId);
        
        Trade trade = tradeRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("거래를 찾을 수 없습니다."));
        
        // 1. 현재 Trade 상태 변경
        trade.setStatus(Trade.TradeStatus.COMPLETED);
        trade.setCompletedAt(LocalDateTime.now());
        tradeRepository.save(trade);
        log.info("✅ 현재 거래 완료 처리 - tradeId: {}", trade.getTradeId());
        
        // 2. Posts 상태 변경
        Posts post = postRepository.findById(postId.intValue())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        post.setStatus("SOLD");
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
        log.info("✅ 상품 상태 변경 - postId: {}, status: SOLD", postId);
        
        // 3. 같은 상품의 다른 모든 Trade, ChatRoom, ChatMessage 삭제
        java.util.List<Trade> otherTrades = tradeRepository.findAllByPostId(postId);
        int deletedCount = 0;
        for (Trade otherTrade : otherTrades) {
            // 현재 Trade가 아니고, 아직 IN_PROGRESS 상태인 경우만
            if (!otherTrade.getTradeId().equals(trade.getTradeId()) && 
                otherTrade.getStatus() == Trade.TradeStatus.IN_PROGRESS) {
                
                String otherRoomId = otherTrade.getRoomId();
                
                // 3-1. 채팅 메시지 삭제
                try {
                    chatMessageRepository.deleteByRoomId(otherRoomId);
                    log.info("✅ 채팅 메시지 삭제 완료 - roomId: {}", otherRoomId);
                } catch (Exception e) {
                    log.error("❌ 채팅 메시지 삭제 실패: {}", e.getMessage());
                }
                
                // 3-2. 채팅방 삭제
                try {
                    chatRoomRepository.findByRoomId(otherRoomId).ifPresent(room -> {
                        chatRoomRepository.delete(room);
                        log.info("✅ 채팅방 삭제 완료 - roomId: {}", otherRoomId);
                    });
                } catch (Exception e) {
                    log.error("❌ 채팅방 삭제 실패: {}", e.getMessage());
                }
                
                // 3-3. Trade 삭제
                tradeRepository.delete(otherTrade);
                deletedCount++;
                log.info("✅ Trade 삭제 완료 - tradeId: {}", otherTrade.getTradeId());
            }
        }
        
        if (deletedCount > 0) {
            log.info("✅ 같은 상품의 다른 거래들 모두 삭제 완료 - 개수: {}", deletedCount);
        }
        
        return trade;
    }
    
    // roomId로 거래 조회
    @Transactional(readOnly = true)
    public Trade getTradeByRoomId(String roomId) {
        return tradeRepository.findByRoomId(roomId).orElse(null);
    }
    
    // 판매 횟수 (판매자로서 거래 완료한 횟수)
    @Transactional(readOnly = true)
    public Long countCompletedSales(String sellerId) {
        return tradeRepository.countBySellerIdAndStatus(sellerId, Trade.TradeStatus.COMPLETED);
    }
    
    // 구매 횟수 (구매자로서 거래 완료한 횟수)
    @Transactional(readOnly = true)
    public Long countCompletedPurchases(String buyerId) {
        return tradeRepository.countByBuyerIdAndStatus(buyerId, Trade.TradeStatus.COMPLETED);
    }
}