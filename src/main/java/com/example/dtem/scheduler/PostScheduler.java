package com.example.dtem.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.dtem.entity.Posts;
import com.example.dtem.repository.PostRepository;

/**
 * 게시글 자동 삭제 스케줄러
 * - SOLD 상태가 된 후 12시간이 지난 게시글을 자동으로 삭제
 */
@Component
public class PostScheduler {

    @Autowired
    private PostRepository postRepository;

    /**
     * ❌ 비활성화: SOLD 상품은 DB에 유지
     * 거래완료 후에도 기록을 남기기 위해 자동 삭제 스케줄러 비활성화
     * index와 postList에서만 SOLD 상품을 필터링하여 표시하지 않음
     */
    // @Scheduled(cron = "0 0 * * * *") // 비활성화
    public void deleteSoldPosts() {
        // 사용하지 않음 - SOLD 상품은 DB에 영구 보관
    }
}

