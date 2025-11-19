package com.example.dtem.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    
    @Override
    protected Principal determineUser(ServerHttpRequest request, 
                                     WebSocketHandler wsHandler, 
                                     Map<String, Object> attributes) {
        // 실제 환경에서는 JWT 토큰이나 세션에서 사용자 정보를 가져옵니다
        // 여기서는 간단히 UUID를 사용
        String userId = request.getURI().getQuery();
        if (userId != null && userId.startsWith("userId=")) {
            userId = userId.substring(7);
        } else {
            userId = UUID.randomUUID().toString();
        }
        
        final String finalUserId = userId;
        return new Principal() {
            @Override
            public String getName() {
                return finalUserId;
            }
        };
    }
}