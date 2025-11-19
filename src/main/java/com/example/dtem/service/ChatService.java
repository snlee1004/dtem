package com.example.dtem.service;

import com.example.dtem.dto.ChatMessageDTO;
import com.example.dtem.entity.ChatMessage;
import com.example.dtem.entity.ChatRoom;
import com.example.dtem.repository.ChatMessageRepository;
import com.example.dtem.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    
    // 메시지 저장
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO messageDTO) {
        log.info("💬 메시지 저장 시작 - sender: {}, receiver: {}, content: {}", 
                 messageDTO.getSenderId(), messageDTO.getReceiverId(), messageDTO.getContent());
        
        messageDTO.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(messageDTO.toEntity());
        
        log.info("✅ 메시지 DB 저장 완료 - id: {}", saved.getId());
        
        // ⭐ 마지막 메시지 업데이트는 ChatController에서 roomId로 처리
        
        return ChatMessageDTO.fromEntity(saved);
    }
    
    // 채팅방의 마지막 메시지 업데이트
    @Transactional
    public void updateChatRoomLastMessage(String senderId, String receiverId, String lastMessage) {
        log.info("🔄 채팅방 업데이트 시도 - sender: {}, receiver: {}", senderId, receiverId);
        
        // 두 사용자 간의 채팅방 찾기
        var roomOptional = chatRoomRepository.findByUsers(senderId, receiverId);
        
        if (roomOptional.isPresent()) {
            ChatRoom room = roomOptional.get();
            log.info("✅ 채팅방 찾음 - roomId: {}, 이전 메시지: {}", room.getRoomId(), room.getLastMessage());
            
            room.setLastMessage(lastMessage);
            room.setLastMessageAt(LocalDateTime.now());
            chatRoomRepository.save(room);
            
            log.info("✅ 채팅방 업데이트 완료 - 새 메시지: {}", lastMessage);
        } else {
            log.warn("⚠️ 채팅방을 찾을 수 없음 - sender: {}, receiver: {}", senderId, receiverId);
        }
    }
    
    // 대화 내역 조회
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(String user1, String user2) {
        return chatMessageRepository.findChatHistory(user1, user2)
                .stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    // 안 읽은 메시지 개수
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(String receiverId, String senderId) {
        return chatMessageRepository.countUnreadMessages(receiverId, senderId);
    }
    
    // 메시지 읽음 처리 (사용자 기준 - 레거시)
    @Transactional
    public void markAsRead(String receiverId, String senderId) {
        chatMessageRepository.markMessagesAsRead(receiverId, senderId);
    }
    
    // 메시지 읽음 처리 (채팅방 기준)
    @Transactional
    public void markAsReadByRoomId(String roomId, String receiverId) {
        log.info("📖 메시지 읽음 처리 - roomId: {}, receiverId: {}", roomId, receiverId);
        chatMessageRepository.markMessagesAsReadByRoomId(roomId, receiverId);
        log.info("✅ 읽음 처리 완료");
    }
    
    // 최근 대화 상대 목록
    @Transactional(readOnly = true)
    public List<String> getRecentChatPartners(String userId) {
        return chatMessageRepository.findRecentChatPartners(userId);
    }
    
    // roomId로 메시지 목록 조회
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        log.info("📨 채팅방 메시지 조회 - roomId: {}", roomId);
        
        // roomId로 직접 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);
        
        log.info("✅ 메시지 {}개 조회 완료", messages.size());
        return messages;
    }
}