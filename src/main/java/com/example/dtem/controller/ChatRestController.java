package com.example.dtem.controller;

import com.example.dtem.dto.ChatMessageDTO;
import com.example.dtem.dto.ChatRoomDTO;
import com.example.dtem.service.ChatRoomService;
import com.example.dtem.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {
    
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    
    // 대화 내역 조회
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @RequestParam("user1") String user1,
            @RequestParam("user2") String user2)  {
        List<ChatMessageDTO> history = chatService.getChatHistory(user1, user2);
        return ResponseEntity.ok(history);
    }
    
    // 안 읽은 메시지 개수
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestParam("receiverId") String receiverId,
            @RequestParam("senderId") String senderId) {
        Long count = chatService.getUnreadMessageCount(receiverId, senderId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
    
    // 최근 대화 상대 목록
    @GetMapping("/recent-partners")
    public ResponseEntity<List<String>> getRecentPartners(
            @RequestParam("userId") String userId) {
        List<String> partners = chatService.getRecentChatPartners(userId);
        return ResponseEntity.ok(partners);
    }
    
    // 메시지 읽음 처리
    @PostMapping("/mark-as-read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam("receiverId") String receiverId,
            @RequestParam("senderId") String senderId) {
        chatService.markAsRead(receiverId, senderId);
        return ResponseEntity.ok().build();
    }
    
    // ========== 새로 추가된 API ==========
    
    // 사용자의 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(
            @RequestParam("userId") String userId) {
        List<ChatRoomDTO> rooms = chatRoomService.getUserChatRooms(userId);
        return ResponseEntity.ok(rooms);
    }
    
    // 상품별 채팅방 목록 조회 (판매자용)
    @GetMapping("/rooms/product/{postId}")
    public ResponseEntity<List<ChatRoomDTO>> getProductChatRooms(
            @PathVariable("postId") Long postId,
            @RequestParam("sellerId") String sellerId) {
        List<ChatRoomDTO> rooms = chatRoomService.getProductChatRooms(postId, sellerId);
        return ResponseEntity.ok(rooms);
    }
    
    // 채팅방 생성 또는 가져오기
    @PostMapping("/rooms/create")
    public ResponseEntity<ChatRoomDTO> createOrGetChatRoom(
            @RequestParam("postId") Long postId,
            @RequestParam("sellerId") String sellerId,
            @RequestParam("buyerId") String buyerId) {
        ChatRoomDTO room = chatRoomService.getOrCreateChatRoomForPost(postId, sellerId, buyerId);
        return ResponseEntity.ok(room);
    }
    
    // 특정 채팅방 정보 조회
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(
            @PathVariable("roomId") String roomId,
            @RequestParam("userId") String userId) {
        ChatRoomDTO room = chatRoomService.getChatRoomByRoomId(roomId, userId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }
}