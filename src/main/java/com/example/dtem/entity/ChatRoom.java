package com.example.dtem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"chat_room\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_room_seq")
    @SequenceGenerator(name = "chat_room_seq", sequenceName = "CHAT_ROOM_SEQ", allocationSize = 1)
    @Column(name = "\"id\"")
    private Long id;
    
    @Column(name = "\"room_id\"", nullable = false, unique = true)
    private String roomId;
    
    @Column(name = "\"user1_id\"", nullable = false)
    private String user1Id;
    
    @Column(name = "\"user2_id\"", nullable = false)
    private String user2Id;
    
    @Column(name = "\"post_id\"")
    private Long postId;
    
    @Column(name = "\"created_at\"", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "\"last_message_at\"")
    private LocalDateTime lastMessageAt;
    
    @Column(name = "\"last_message\"", length = 2000)
    private String lastMessage;
    
    @Column(name = "\"user1_left\"")
    private Boolean user1Left = false;  // user1이 채팅방을 나갔는지
    
    @Column(name = "\"user2_left\"")
    private Boolean user2Left = false;  // user2가 채팅방을 나갔는지
    
    // 두 사용자 ID로 고유한 roomId 생성
    public static String generateRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }
    
    // 상품 기반 roomId 생성
    public static String generateRoomIdWithPost(Long postId, String userId1, String userId2) {
        String baseRoomId = generateRoomId(userId1, userId2);
        return postId + "_" + baseRoomId;
    }
}


