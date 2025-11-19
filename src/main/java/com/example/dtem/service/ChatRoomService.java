package com.example.dtem.service;

import com.example.dtem.dto.ChatRoomDTO;
import com.example.dtem.entity.ChatRoom;
import com.example.dtem.entity.Trade;
import com.example.dtem.entity.Users;
import com.example.dtem.entity.Posts;
import com.example.dtem.repository.ChatMessageRepository;
import com.example.dtem.repository.ChatRoomRepository;
import com.example.dtem.repository.TradeRepository;
import com.example.dtem.repository.UserRepository;
import com.example.dtem.repository.PostRepository;
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
public class ChatRoomService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    
    // 상품 기반 채팅방 생성 또는 가져오기
    @Transactional
    public ChatRoomDTO getOrCreateChatRoomForPost(Long postId, String sellerId, String buyerId) {
        log.info("🔍 채팅방 검색 시작 - postId: {}, sellerId: {}, buyerId: {}", postId, sellerId, buyerId);
        
        return chatRoomRepository.findByPostIdAndUsers(postId, sellerId, buyerId)
                .map(room -> {
                    log.info("✅ 기존 채팅방 발견 - roomId: {}, postId: {}", room.getRoomId(), room.getPostId());
                    
                    // 구매자가 다시 입장한 경우 Left 플래그 해제
                    if (Boolean.TRUE.equals(room.getUser2Left()) && room.getUser2Id().equals(buyerId)) {
                        log.info("🔄 구매자 재입장");
                        room.setUser2Left(false);
                        chatRoomRepository.save(room);
                    }
                    
                    return convertToDTO(room, buyerId);
                })
                .orElseGet(() -> {
                    String roomId = ChatRoom.generateRoomIdWithPost(postId, sellerId, buyerId);
                    log.info("🆕 새 채팅방 생성 - roomId: {}, postId: {}", roomId, postId);
                    
                    ChatRoom newRoom = ChatRoom.builder()
                            .roomId(roomId)
                            .user1Id(sellerId)
                            .user2Id(buyerId)
                            .postId(postId)
                            .createdAt(LocalDateTime.now())
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    
                    ChatRoom savedRoom = chatRoomRepository.save(newRoom);
                    log.info("💾 채팅방 DB 저장 완료 - id: {}, roomId: {}", savedRoom.getId(), savedRoom.getRoomId());
                    
                    return convertToDTO(savedRoom, buyerId);
                });
    }
    
    // 사용자의 모든 채팅방 목록 조회 (마이페이지용)
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getUserChatRooms(String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserId(userId);
        
        return rooms.stream()
                .filter(room -> {
                    // 현재 유저가 나간 채팅방은 목록에서 제외
                    boolean isUser1 = room.getUser1Id().equals(userId);
                    if (isUser1 && Boolean.TRUE.equals(room.getUser1Left())) {
                        return false; // user1이 나간 경우 제외
                    }
                    if (!isUser1 && Boolean.TRUE.equals(room.getUser2Left())) {
                        return false; // user2가 나간 경우 제외
                    }
                    return true;
                })
                .map(room -> convertToDTO(room, userId))
                .collect(Collectors.toList());
    }
    
    // 특정 상품의 채팅방 목록 조회 (판매자가 상품 페이지에서 보는 용)
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getProductChatRooms(Long postId, String sellerId) {
        List<ChatRoom> rooms = chatRoomRepository.findByPostId(postId);
        
        return rooms.stream()
                .filter(room -> {
                    // 판매자(user1)가 나간 채팅방은 제외
                    if (room.getUser1Id().equals(sellerId) && Boolean.TRUE.equals(room.getUser1Left())) {
                        return false;
                    }
                    return true;
                })
                .map(room -> convertToDTO(room, sellerId))
                .collect(Collectors.toList());
    }
    
    // 채팅방 정보 업데이트 (마지막 메시지)
    @Transactional
    public void updateLastMessage(String roomId, String lastMessage) {
        chatRoomRepository.findByRoomId(roomId).ifPresent(room -> {
            room.setLastMessage(lastMessage);
            room.setLastMessageAt(LocalDateTime.now());
            chatRoomRepository.save(room);
        });
    }
    
    // roomId로 채팅방 조회
    @Transactional(readOnly = true)
    public ChatRoomDTO getChatRoomByRoomId(String roomId, String currentUserId) {
        log.info("🔍 채팅방 조회 - roomId: {}, currentUserId: {}", roomId, currentUserId);
        
        java.util.Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
        log.info("📌 조회 결과 - isPresent: {}", roomOpt.isPresent());
        
        if (roomOpt.isEmpty()) {
            log.warn("⚠️ 채팅방을 찾을 수 없음 - roomId: {}", roomId);
            return null;
        }
        
        return roomOpt.map(room -> convertToDTO(room, currentUserId)).get();
    }
    
    private ChatRoomDTO convertToDTO(ChatRoom room, String currentUserId) {
        // 상대방 ID 결정
        String partnerId = room.getUser1Id().equals(currentUserId) ? 
                          room.getUser2Id() : room.getUser1Id();
        
        // 안 읽은 메시지 개수 조회 (roomId 기준으로 변경)
        Long unreadCount = chatMessageRepository.countUnreadMessagesByRoomId(room.getRoomId(), currentUserId);
        
        // 현재 사용자가 판매자인지 확인 (user1Id가 판매자로 가정)
        boolean isSeller = room.getUser1Id().equals(currentUserId);
        
        // 상대방 정보 조회
        String partnerName = null;
        String partnerProfileImage = null;
        try {
            Integer partnerIdInt = Integer.parseInt(partnerId);
            Users partner = userRepository.findById(partnerIdInt).orElse(null);
            if (partner != null) {
                partnerName = partner.getUsername();
                partnerProfileImage = partner.getProfileImage();
            }
        } catch (NumberFormatException e) {
            // partnerId를 Integer로 변환할 수 없는 경우 무시
        }
        
        // 상품 정보 조회
        String postTitle = null;
        String postImage = null;
        String postStatus = null;
        if (room.getPostId() != null) {
            Integer postIdInt = room.getPostId().intValue();
            Posts post = postRepository.findById(postIdInt).orElse(null);
            if (post != null) {
                postTitle = post.getTitle();
                postStatus = post.getStatus();
                // 첫 번째 이미지 추출
                if (post.getPostImage() != null && !post.getPostImage().isEmpty()) {
                    String[] images = post.getPostImage().split(",");
                    if (images.length > 0) {
                        postImage = images[0].trim();
                    }
                }
            }
        }
        
        // 거래 상태 조회
        String tradeStatus = null;
        Trade trade = tradeRepository.findByRoomId(room.getRoomId()).orElse(null);
        if (trade != null) {
            tradeStatus = trade.getStatus().name();
        }
        
        return ChatRoomDTO.builder()
                .id(room.getId())
                .roomId(room.getRoomId())
                .user1Id(room.getUser1Id())
                .user2Id(room.getUser2Id())
                .partnerId(partnerId)
                .partnerName(partnerName)
                .partnerProfileImage(partnerProfileImage)
                .postId(room.getPostId())
                .postTitle(postTitle)
                .postImage(postImage)
                .postStatus(postStatus)
                .tradeStatus(tradeStatus)
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .lastMessage(room.getLastMessage())
                .unreadCount(unreadCount)
                .isSeller(isSeller)
                .user1Left(room.getUser1Left())
                .user2Left(room.getUser2Left())
                .build();
    }
    
    // 채팅방 나가기 처리
    @Transactional
    public void leaveChat(String roomId, String userId) {
        log.info("🚪 채팅방 나가기 처리 - roomId: {}, userId: {}", roomId, userId);
        
        // 채팅방 존재 여부 디버깅
        java.util.Optional<ChatRoom> roomOpt = chatRoomRepository.findByRoomId(roomId);
        log.info("🔍 채팅방 조회 결과 - isPresent: {}", roomOpt.isPresent());
        
        ChatRoom room = roomOpt.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        
        // 기존 null 값 초기화 (기존 DB 레코드 대응)
        if (room.getUser1Left() == null) {
            room.setUser1Left(false);
        }
        if (room.getUser2Left() == null) {
            room.setUser2Left(false);
        }
        
        // 나간 사용자 확인
        boolean isUser1 = room.getUser1Id().equals(userId);
        
        // 사용자 이름 조회
        String username = "사용자";
        try {
            Integer userIdInt = Integer.parseInt(userId);
            Users user = userRepository.findById(userIdInt).orElse(null);
            if (user != null) {
                username = user.getUsername();
            }
        } catch (NumberFormatException e) {
            // 무시
        }
        
        // 🆕 구매자(user2)가 나가면 모든 데이터 삭제
        if (!isUser1) {
            log.info("🗑️ 구매자가 나감 - Trade, 채팅 메시지, 채팅방 모두 삭제");
            
            // 1. Trade 삭제
            try {
                Trade trade = tradeRepository.findByRoomId(roomId).orElse(null);
                if (trade != null) {
                    tradeRepository.delete(trade);
                    log.info("✅ Trade 삭제 완료 - tradeId: {}", trade.getTradeId());
                }
            } catch (Exception e) {
                log.error("❌ Trade 삭제 실패: {}", e.getMessage());
            }
            
            // 2. 채팅 메시지 삭제
            try {
                chatMessageRepository.deleteByRoomId(roomId);
                log.info("✅ 채팅 메시지 삭제 완료 - roomId: {}", roomId);
            } catch (Exception e) {
                log.error("❌ 채팅 메시지 삭제 실패: {}", e.getMessage());
            }
            
            // 3. 채팅방 삭제
            chatRoomRepository.delete(room);
            log.info("✅ 채팅방 삭제 완료 - 구매자 {}님이 나감", username);
            
        } else {
            // 판매자(user1)가 나가면 플래그만 설정
            room.setUser1Left(true);
            chatRoomRepository.save(room);
            log.info("✅ 판매자 {}님이 채팅방을 나감 (플래그 설정)", username);
        }
    }
}