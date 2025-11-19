package com.example.dtem.repository;

import com.example.dtem.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 두 사용자 간의 대화 내역 조회
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :user1 AND m.receiverId = :user2) OR " +
           "(m.senderId = :user2 AND m.receiverId = :user1) " +
           "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("user1") String user1, 
                                       @Param("user2") String user2);
    
    // 안 읽은 메시지 개수 조회 (사용자 기준 - 레거시)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
           "m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false")
    Long countUnreadMessages(@Param("receiverId") String receiverId, 
                              @Param("senderId") String senderId);
    
    // 안 읽은 메시지 개수 조회 (채팅방 기준 - 현재 사용자가 받은 메시지)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE " +
           "m.roomId = :roomId AND m.receiverId = :receiverId AND m.isRead = false")
    Long countUnreadMessagesByRoomId(@Param("roomId") String roomId, 
                                      @Param("receiverId") String receiverId);
    
    // 메시지 읽음 처리 (사용자 기준 - 레거시)
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE " +
           "m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false")
    void markMessagesAsRead(@Param("receiverId") String receiverId, 
                            @Param("senderId") String senderId);
    
    // 메시지 읽음 처리 (채팅방 기준 - 현재 사용자가 받은 메시지)
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE " +
           "m.roomId = :roomId AND m.receiverId = :receiverId AND m.isRead = false")
    void markMessagesAsReadByRoomId(@Param("roomId") String roomId, 
                                     @Param("receiverId") String receiverId);
    
    // 특정 채팅방의 메시지 조회
    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId ORDER BY m.timestamp ASC")
    List<ChatMessage> findByRoomId(@Param("roomId") String roomId);
    
    // 사용자의 최근 대화 목록 조회
    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId " +
           "ELSE m.senderId END FROM ChatMessage m WHERE " +
           "m.senderId = :userId OR m.receiverId = :userId")
    List<String> findRecentChatPartners(@Param("userId") String userId);
    
    // 특정 채팅방의 모든 메시지 삭제
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") String roomId);
}


