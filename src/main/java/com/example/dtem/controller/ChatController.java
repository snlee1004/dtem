package com.example.dtem.controller;

import com.example.dtem.dto.ChatMessageDTO;
import com.example.dtem.dto.ChatRoomDTO;
import com.example.dtem.entity.ChatMessage;
import com.example.dtem.entity.ChatRoom;
import com.example.dtem.entity.Posts;
import com.example.dtem.entity.Trade;
import com.example.dtem.entity.Users;
import com.example.dtem.repository.ChatRoomRepository;
import com.example.dtem.repository.PostRepository;
import com.example.dtem.repository.UserRepository;
import com.example.dtem.service.ChatRoomService;
import com.example.dtem.service.ChatService;
import com.example.dtem.service.TradeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;
    private final TradeService tradeService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    
    // 마이페이지 - 내 채팅방 목록
    @GetMapping("/mypage/chatrooms")
    public String myChatRooms(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/user/loginForm";
        }
        
        String userIdStr = String.valueOf(userId);
        List<ChatRoomDTO> chatRooms = chatRoomService.getUserChatRooms(userIdStr);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("currentUserId", userIdStr);
        
        return "chat/my-chatrooms";
    }
    
    // 상품 페이지 - 판매자용 채팅방 목록
    @GetMapping("/product/chatrooms")
    public String productChatRooms(@RequestParam("postId") Long postId, 
                                   HttpSession session, 
                                   Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/user/loginForm";
        }
        
        // 상품 정보 조회
        Posts post = postRepository.findById(postId.intValue())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        
        String userIdStr = String.valueOf(userId);
        List<ChatRoomDTO> chatRooms = chatRoomService.getProductChatRooms(postId, userIdStr);
        
        model.addAttribute("post", post);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("postId", postId);
        model.addAttribute("currentUserId", userIdStr);
        
        return "chat/product-chatrooms";
    }
    
    // 상품 페이지 - 구매자용 (바로 채팅방 열기)
    @GetMapping("/product/chat")
    public String startChatWithSeller(@RequestParam("postId") Long postId,
                                      @RequestParam("sellerId") Integer sellerId,
                                      HttpSession session,
                                      Model model) {
        Integer buyerId = (Integer) session.getAttribute("userId");
        
        if (buyerId == null) {
            return "redirect:/user/loginForm";
        }
        
        // Integer를 String으로 변환
        String sellerIdStr = String.valueOf(sellerId);
        String buyerIdStr = String.valueOf(buyerId);
        
        log.info("🚀 채팅 시작 요청 - postId: {}, sellerId: {}, buyerId: {}", postId, sellerIdStr, buyerIdStr);
        
        // 채팅방 생성 또는 가져오기
        ChatRoomDTO chatRoom = chatRoomService.getOrCreateChatRoomForPost(postId, sellerIdStr, buyerIdStr);
        
        log.info("📱 채팅방 결과 - roomId: {}, postId: {}", chatRoom.getRoomId(), chatRoom.getPostId());
        
        // 거래가 없으면 생성
        Trade trade = tradeService.getTradeByRoomId(chatRoom.getRoomId());
        if (trade == null) {
            tradeService.createTrade(postId, sellerIdStr, buyerIdStr, chatRoom.getRoomId());
            log.info("💼 새 거래 생성 - postId: {}, roomId: {}", postId, chatRoom.getRoomId());
        }
        
        return "redirect:/chat/room?roomId=" + chatRoom.getRoomId();
    }
    
    // 채팅방 페이지
    @GetMapping("/chat/room")
    public String chatRoom(@RequestParam("roomId") String roomId,
                          HttpSession session,
                          Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/user/loginForm";
        }
        
        String userIdStr = String.valueOf(userId);
        ChatRoomDTO chatRoom = chatRoomService.getChatRoomByRoomId(roomId, userIdStr);
        Trade trade = tradeService.getTradeByRoomId(roomId);
        
        // ChatRoom 엔티티 가져오기 (나가기 상태 확인용)
        ChatRoom chatRoomEntity = chatRoomRepository.findByRoomId(roomId).orElse(null);
        
        // 채팅방 입장 시 해당 채팅방의 메시지를 읽음 처리
        chatService.markAsReadByRoomId(roomId, userIdStr);
        
        // 상품 정보 조회
        Posts post = null;
        if (chatRoom.getPostId() != null) {
            post = postRepository.findById(chatRoom.getPostId().intValue()).orElse(null);
        }
        
        // 상대방 정보 조회
        Users partner = null;
        if (chatRoom.getPartnerId() != null) {
            try {
                Integer partnerIdInt = Integer.parseInt(chatRoom.getPartnerId());
                partner = userRepository.findById(partnerIdInt).orElse(null);
            } catch (NumberFormatException e) {
                // 무시
            }
        }
        
        // 현재 사용자 정보 조회
        Users currentUser = userRepository.findById(userId).orElse(null);
        
        // 채팅방 상태 확인
        String tradeStatus = trade != null ? trade.getStatus().name() : null;
        boolean chatDisabled = false;
        String disabledMessage = null;
        
        // 거래완료/취소 확인
        if ("COMPLETED".equals(tradeStatus)) {
            chatDisabled = true;
            disabledMessage = "거래가 완료된 상품입니다.";
        } else if ("CANCELLED".equals(tradeStatus)) {
            chatDisabled = true;
            disabledMessage = "거래가 취소된 상품입니다.";
        } else if (chatRoomEntity != null) {
            // 상대방이 나갔는지 확인
            boolean isUser1 = chatRoomEntity.getUser1Id().equals(userIdStr);
            boolean partnerLeft = false;
            
            if (isUser1 && Boolean.TRUE.equals(chatRoomEntity.getUser2Left())) {
                partnerLeft = true; // user2(상대방)가 나감
            } else if (!isUser1 && Boolean.TRUE.equals(chatRoomEntity.getUser1Left())) {
                partnerLeft = true; // user1(상대방)가 나감
            }
            
            if (partnerLeft) {
                chatDisabled = true;
                String partnerName = partner != null ? partner.getUsername() : "상대방";
                disabledMessage = partnerName + "님이 채팅방을 나갔습니다.";
            }
        }
        
        model.addAttribute("roomId", roomId);
        model.addAttribute("currentUserId", userIdStr);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("partner", partner);
        model.addAttribute("post", post);
        model.addAttribute("isSeller", chatRoom.isSeller());
        model.addAttribute("tradeId", trade != null ? trade.getTradeId() : null);
        model.addAttribute("tradeStatus", tradeStatus);
        model.addAttribute("chatDisabled", chatDisabled);
        model.addAttribute("disabledMessage", disabledMessage);
        
        return "chat/chatroom";
    }
    
    // 거래 완료 처리 (POST - JSON)
    @PostMapping("/chat/complete-trade")
    @ResponseBody
    public Map<String, Object> completeTradePost(@RequestBody Map<String, String> request,
                                                  HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            
            String roomId = request.get("roomId");
            Long postId = Long.parseLong(request.get("postId"));
            String userIdStr = String.valueOf(userId);
            
            log.info("💼 거래완료 요청 - postId: {}, roomId: {}, userId: {}", postId, roomId, userIdStr);
            
            // 채팅방 정보 조회
            ChatRoomDTO chatRoom = chatRoomService.getChatRoomByRoomId(roomId, userIdStr);
            if (chatRoom == null) {
                throw new RuntimeException("채팅방을 찾을 수 없습니다.");
            }
            
            // Trade 존재 여부 확인
            Trade existingTrade = tradeService.getTradeByRoomId(roomId);
            if (existingTrade == null) {
                log.warn("⚠️ Trade가 없음 - 새로 생성");
                String partnerId = chatRoom.getPartnerId();
                String sellerId = chatRoom.isSeller() ? userIdStr : partnerId;
                String buyerId = chatRoom.isSeller() ? partnerId : userIdStr;
                tradeService.createTrade(postId, sellerId, buyerId, roomId);
            }
            
            // 거래 완료 처리
            Trade trade = tradeService.completeTrade(roomId, postId);
            
            // 리뷰 페이지 URL 생성
            String reviewUrl = "/user/userReview?postId=" + postId + 
                             "&revieweeId=" + chatRoom.getPartnerId() + 
                             "&tradeId=" + trade.getTradeId();
            
            response.put("success", true);
            response.put("reviewUrl", reviewUrl);
            
            log.info("✅ 거래완료 처리 성공");
            
        } catch (Exception e) {
            log.error("❌ 거래완료 처리 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // 채팅방 나가기 처리
    @PostMapping("/chat/leave")
    @ResponseBody
    public Map<String, Object> leaveChat(@RequestBody Map<String, String> request,
                                         HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            
            String roomId = request.get("roomId");
            String userIdStr = String.valueOf(userId);
            
            log.info("🚪 채팅방 나가기 요청 - roomId: {}, userId: {}", roomId, userIdStr);
            
            // 채팅방 나가기 처리
            chatRoomService.leaveChat(roomId, userIdStr);
            
            response.put("success", true);
            
            log.info("✅ 채팅방 나가기 성공");
            
        } catch (Exception e) {
            log.error("❌ 채팅방 나가기 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // 채팅 메시지 목록 조회 API
    @GetMapping("/chat/messages")
    @ResponseBody
    public List<ChatMessage> getChatMessages(@RequestParam("roomId") String roomId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return List.of();
        }
        
        return chatService.getMessagesByRoomId(roomId);
    }
    
    // ==================== WebSocket 메시지 핸들러 ====================
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO message) {
        // 메시지 저장
        ChatMessageDTO savedMessage = chatService.saveMessage(message);
        
        // 해당 채팅방의 모든 참여자에게 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(),
                savedMessage
        );
        
        // 채팅방의 마지막 메시지 업데이트
        chatRoomService.updateLastMessage(message.getRoomId(), message.getContent());
    }
    
    @MessageMapping("/chat.typing")
    public void sendTypingNotification(@Payload ChatMessageDTO message) {
        message.setType(ChatMessage.MessageType.TYPING);
        
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/queue/typing",
                message
        );
    }
    
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ChatMessageDTO message) {
        chatService.markAsRead(message.getReceiverId(), message.getSenderId());
        
        messagingTemplate.convertAndSendToUser(
                message.getSenderId(),
                "/queue/read",
                message
        );
    }
}