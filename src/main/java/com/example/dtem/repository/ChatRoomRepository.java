package com.example.dtem.repository;

import com.example.dtem.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // roomId로 채팅방 찾기
    Optional<ChatRoom> findByRoomId(String roomId);
    
    // 특정 사용자가 참여한 모든 채팅방 조회
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1Id = :userId OR cr.user2Id = :userId ORDER BY cr.lastMessageAt DESC NULLS LAST")
    List<ChatRoom> findByUserId(@Param("userId") String userId);
    
    // 두 사용자 간 채팅방 존재 여부 확인 (순서 상관없이)
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "(cr.user1Id = :user1 AND cr.user2Id = :user2) OR " +
           "(cr.user1Id = :user2 AND cr.user2Id = :user1)")
    Optional<ChatRoom> findByUsers(@Param("user1") String user1, @Param("user2") String user2);
    
    // 특정 상품의 채팅방 조회 (판매자용)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.postId = :postId ORDER BY cr.lastMessageAt DESC NULLS LAST")
    List<ChatRoom> findByPostId(@Param("postId") Long postId);
    
    // 특정 상품과 사용자 간 채팅방 찾기
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.postId = :postId AND " +
           "((cr.user1Id = :user1 AND cr.user2Id = :user2) OR " +
           "(cr.user1Id = :user2 AND cr.user2Id = :user1))")
    Optional<ChatRoom> findByPostIdAndUsers(@Param("postId") Long postId,
                                            @Param("user1") String user1,
                                            @Param("user2") String user2);
}


