package com.example.dtem.controller;

import com.example.dtem.dto.ChatRoomDTO;
import com.example.dtem.entity.Trade;
import com.example.dtem.service.ChatRoomService;
import com.example.dtem.service.TradeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    
    private final ChatRoomService chatRoomService;
    private final TradeService tradeService;
    
    // 테스트용: 로그인 (세션에 userId 저장)
    @GetMapping("/login")
    @ResponseBody
    public String testLogin(@RequestParam(value = "userId", defaultValue = "1") String userId, 
                           HttpSession session) {
        session.setAttribute("userId", userId);
        
        return String.format(
            "✅ 로그인 성공!<br><br>" +
            "userId: <strong>%s</strong><br><br>" +
            "<a href='/test/menu'>테스트 메뉴로 이동</a>",
            userId
        );
    }
    
    // 테스트 메뉴
    @GetMapping("/menu")
    @ResponseBody
    public String testMenu(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            return "❌ 로그인이 필요합니다!<br><br>" +
                   "<a href='/test/login?userId=1'>사용자1로 로그인</a><br>" +
                   "<a href='/test/login?userId=2'>사용자2로 로그인</a>";
        }
        
        return String.format(
            "<h2>🧪 채팅 테스트 메뉴</h2>" +
            "현재 로그인: <strong>%s</strong><br><br>" +
            
            "<h3>1. 기본 테스트</h3>" +
            "<a href='/test/check-roomid'>roomId 생성 규칙 확인</a><br>" +
            "<a href='/test/create-chatroom'>채팅방 생성</a><br>" +
            "<a href='/test/list-chatrooms'>내 채팅방 목록</a><br><br>" +
            
            "<h3>2. 채팅 시나리오 테스트</h3>" +
            "<a href='/test/scenario/buyer'>구매자 시나리오 (userId=1)</a><br>" +
            "<a href='/test/scenario/seller'>판매자 시나리오 (userId=2)</a><br><br>" +
            
            "<h3>3. 로그인 변경</h3>" +
            "<a href='/test/login?userId=1'>사용자1로 재로그인</a><br>" +
            "<a href='/test/login?userId=2'>사용자2로 재로그인</a><br><br>" +
            
            "<h3>4. 직접 채팅방 입장</h3>" +
            "<form action='/test/enter-room' method='get'>" +
            "roomId: <input type='text' name='roomId' value='1_1_2' />" +
            "<button type='submit'>입장</button>" +
            "</form>",
            userId
        );
    }
    
    // 테스트용: 채팅방 생성
    @GetMapping("/create-chatroom")
    @ResponseBody
    public String createTestChatRoom() {
        try {
            // postId=1, sellerId="2", buyerId="1"
            ChatRoomDTO chatRoom = chatRoomService.getOrCreateChatRoomForPost(1L, "2", "1");
            
            // 거래 생성
            Trade trade = tradeService.getTradeByRoomId(chatRoom.getRoomId());
            if (trade == null) {
                trade = tradeService.createTrade(1L, "2", "1", chatRoom.getRoomId());
            }
            
            return String.format(
                "✅ 채팅방 생성 완료!<br><br>" +
                "roomId: <strong>%s</strong><br>" +
                "postId: %d<br>" +
                "sellerId: %s (판매자)<br>" +
                "buyerId: %s (구매자)<br>" +
                "tradeId: %d<br><br>" +
                "<a href='/test/enter-room?roomId=%s'>채팅방 입장 (현재 세션 유저로)</a><br>" +
                "<a href='/test/enter-room-as?roomId=%s&userId=1'>구매자(1)로 입장</a><br>" +
                "<a href='/test/enter-room-as?roomId=%s&userId=2'>판매자(2)로 입장</a><br><br>" +
                "<a href='/test/menu'>메뉴로 돌아가기</a>",
                chatRoom.getRoomId(),
                chatRoom.getPostId(),
                chatRoom.getUser1Id(),
                chatRoom.getUser2Id(),
                trade.getTradeId(),
                chatRoom.getRoomId(),
                chatRoom.getRoomId(),
                chatRoom.getRoomId()
            );
            
        } catch (Exception e) {
            return "❌ 에러 발생: " + e.getMessage() + "<br><br>" + 
                   "스택 트레이스:<br>" + getStackTrace(e) +
                   "<br><br><a href='/test/menu'>메뉴로 돌아가기</a>";
        }
    }
    
    // 테스트용: 모든 채팅방 조회
    @GetMapping("/list-chatrooms")
    @ResponseBody
    public String listChatRooms(HttpSession session) {
        try {
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                userId = "1"; // 기본값
            }
            
            var rooms = chatRoomService.getUserChatRooms(userId);
            
            StringBuilder html = new StringBuilder(
                String.format("<h2>✅ %s의 채팅방 목록</h2><br>", userId)
            );
            
            if (rooms.isEmpty()) {
                html.append("채팅방이 없습니다.<br><br>");
                html.append("<a href='/test/create-chatroom'>테스트 채팅방 생성</a><br>");
            } else {
                for (var room : rooms) {
                    html.append(String.format(
                        "<div style='border:1px solid #ccc; padding:10px; margin:10px 0;'>" +
                        "<strong>roomId:</strong> %s<br>" +
                        "<strong>상대방:</strong> %s<br>" +
                        "<strong>상품ID:</strong> %s<br>" +
                        "<strong>안읽은 메시지:</strong> %d개<br>" +
                        "<strong>마지막 메시지:</strong> %s<br>" +
                        "<a href='/test/enter-room?roomId=%s'>입장</a>" +
                        "</div>",
                        room.getRoomId(),
                        room.getPartnerId(),
                        room.getPostId(),
                        room.getUnreadCount(),
                        room.getLastMessage() != null ? room.getLastMessage() : "없음",
                        room.getRoomId()
                    ));
                }
            }
            
            html.append("<br><a href='/test/menu'>메뉴로 돌아가기</a>");
            return html.toString();
            
        } catch (Exception e) {
            return "❌ 에러 발생: " + e.getMessage() + "<br><br>" + 
                   "스택 트레이스:<br>" + getStackTrace(e) +
                   "<br><br><a href='/test/menu'>메뉴로 돌아가기</a>";
        }
    }
    
    // 테스트용: roomId 생성 규칙 확인
    @GetMapping("/check-roomid")
    @ResponseBody
    public String checkRoomId() {
        Long postId = 1L;
        String sellerId = "2";
        String buyerId = "1";
        
        String expectedRoomId = com.example.dtem.entity.ChatRoom.generateRoomIdWithPost(postId, sellerId, buyerId);
        
        return String.format(
            "<h2>✅ roomId 생성 규칙</h2><br>" +
            "postId: %d<br>" +
            "sellerId: %s<br>" +
            "buyerId: %s<br><br>" +
            "생성된 roomId: <strong style='color:red; font-size:20px;'>%s</strong><br><br>" +
            "이 roomId를 사용하세요!<br><br>" +
            "<a href='/test/menu'>메뉴로 돌아가기</a>",
            postId, sellerId, buyerId, expectedRoomId
        );
    }
    
    // 테스트용: 채팅방 입장 (현재 세션 유저)
    @GetMapping("/enter-room")
    public String enterRoom(@RequestParam("roomId") String roomId, 
                           HttpSession session,
                           Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            session.setAttribute("userId", "1");
            userId = "1";
        }
        
        return "redirect:/chat/room?roomId=" + roomId;
    }
    
    // 테스트용: 특정 유저로 채팅방 입장
    @GetMapping("/enter-room-as")
    public String enterRoomAs(@RequestParam("roomId") String roomId,
                             @RequestParam("userId") String userId,
                             HttpSession session) {
        session.setAttribute("userId", userId);
        return "redirect:/chat/room?roomId=" + roomId;
    }
    
    // 구매자 시나리오
    @GetMapping("/scenario/buyer")
    public String buyerScenario(HttpSession session) {
        session.setAttribute("userId", "1");
        return "redirect:/product/chat?postId=1&sellerId=2";
    }
    
    // 판매자 시나리오
    @GetMapping("/scenario/seller")
    @ResponseBody
    public String sellerScenario(HttpSession session) {
        session.setAttribute("userId", "2");
        
        return "<h2>✅ 판매자 시나리오</h2>" +
               "현재 로그인: 사용자2 (판매자)<br><br>" +
               "<a href='/product/chatrooms?postId=1'>상품1의 문의 목록 보기</a><br>" +
               "<a href='/test/menu'>메뉴로 돌아가기</a>";
    }
    
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(10, e.getStackTrace().length); i++) {
            sb.append(e.getStackTrace()[i].toString()).append("<br>");
        }
        return sb.toString();
    }
}