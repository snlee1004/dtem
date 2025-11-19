package com.example.dtem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private String roomId;
    private String user1Id;
    private String user2Id;
    private String partnerId;
    private String partnerName;           // 상대방 이름
    private String partnerProfileImage;    // 상대방 프로필 사진
    private Long postId;
    private String productTitle;
    private String postTitle;              // 상품 제목
    private String postImage;              // 상품 이미지 (첫 번째)
    private String postStatus;             // 상품 상태 (SELLING, SOLD 등)
    private String tradeStatus;            // 거래 상태 (IN_PROGRESS, COMPLETED, CANCELLED)
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private Long unreadCount;
    private boolean isSeller;
    private Boolean user1Left;             // 판매자(user1)가 나갔는지
    private Boolean user2Left;             // 구매자(user2)가 나갔는지
}


