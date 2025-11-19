package com.example.dtem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"trade\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_seq")
    @SequenceGenerator(name = "trade_seq", sequenceName = "TRADE_SEQ", allocationSize = 1)
    @Column(name = "\"trade_id\"")
    private Long tradeId;
    
    @Column(name = "\"post_id\"", nullable = false)
    private Long postId;
    
    @Column(name = "\"seller_id\"", nullable = false)
    private String sellerId;
    
    @Column(name = "\"buyer_id\"", nullable = false)
    private String buyerId;
    
    @Column(name = "\"room_id\"", nullable = false)
    private String roomId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "\"status\"", nullable = false)
    private TradeStatus status;
    
    @Column(name = "\"created_at\"", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "\"completed_at\"")
    private LocalDateTime completedAt;
    
    public enum TradeStatus {
        IN_PROGRESS,  // 거래 중
        COMPLETED,    // 거래 완료
        CANCELLED     // 거래 취소
    }
}


