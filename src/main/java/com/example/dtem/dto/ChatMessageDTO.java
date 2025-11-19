package com.example.dtem.dto;

import com.example.dtem.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    private Long id;
    private String roomId;
    private String senderId;
    private String receiverId;
    private String content;
    private ChatMessage.MessageType type;
    private LocalDateTime timestamp;
    private boolean isRead;
    
    public static ChatMessageDTO fromEntity(ChatMessage entity) {
        return ChatMessageDTO.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .content(entity.getContent())
                .type(entity.getType())
                .timestamp(entity.getTimestamp())
                .isRead(entity.isRead())
                .build();
    }
    
    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .id(this.id)
                .roomId(this.roomId)
                .senderId(this.senderId)
                .receiverId(this.receiverId)
                .content(this.content)
                .type(this.type)
                .timestamp(this.timestamp)
                .isRead(this.isRead)
                .build();
    }
}


