package com.example.dtem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    // 온라인 사용자 관리 (userId -> sessionId)
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getUser() != null ? 
                        headerAccessor.getUser().getName() : null;
        String sessionId = headerAccessor.getSessionId();
        
        if (userId != null) {
            onlineUsers.put(userId, sessionId);
            log.info("사용자 접속: {} (세션: {})", userId, sessionId);
            
            // 온라인 상태 브로드캐스트
            broadcastOnlineStatus(userId, true);
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 세션으로 사용자 찾기
        String disconnectedUser = onlineUsers.entrySet().stream()
                .filter(entry -> entry.getValue().equals(sessionId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        
        if (disconnectedUser != null) {
            onlineUsers.remove(disconnectedUser);
            log.info("사용자 연결 해제: {} (세션: {})", disconnectedUser, sessionId);
            
            // 오프라인 상태 브로드캐스트
            broadcastOnlineStatus(disconnectedUser, false);
        }
    }
    
    private void broadcastOnlineStatus(String userId, boolean isOnline) {
        Map<String, Object> status = Map.of(
            "userId", userId,
            "isOnline", isOnline
        );
        
        messagingTemplate.convertAndSend("/topic/user-status", status);
    }
    
    public boolean isUserOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }
    
    public Map<String, String> getOnlineUsers() {
        return new ConcurrentHashMap<>(onlineUsers);
    }
}	