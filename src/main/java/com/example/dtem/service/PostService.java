package com.example.dtem.service;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.dtem.entity.Posts;
import com.example.dtem.repository.PostRepository;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepo;

    /**
     * ✅ 게시글 목록 (페이징)
     */
    public List<Posts> postList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Posts> postPage = postRepo.findAll(pageable);
        return postPage.getContent();
    }

    /**
     * ✅ 게시글 상세 조회
     */
    public Posts postView(Integer postId) {
        Posts post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return post;
    }

    /**
     * ✅ 조회수 증가
     */
    @Transactional
    public void increaseHit(Integer postId) {
        Posts post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.setHit(post.getHit() + 1);
        postRepo.save(post);
    }

    /**
     * ✅ 게시글 작성
     */
    @Transactional
    public Posts postWrite(Posts post) {
        return postRepo.save(post);
    }

    /**
     * ✅ 게시글 수정
     */
    @Transactional
    public Posts postModify(Integer postId, Posts modifiedPost) {
        Posts post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setTitle(modifiedPost.getTitle());
        post.setContent(modifiedPost.getContent());
        post.setPrice(modifiedPost.getPrice());
        post.setCategory(modifiedPost.getCategory());
        post.setLocation(modifiedPost.getLocation());
        post.setConditionStatus(modifiedPost.getConditionStatus());
        post.setTradeMethod(modifiedPost.getTradeMethod());
        post.setNegotiable(modifiedPost.getNegotiable());

        return postRepo.save(post);
    }

    /**
     * ✅ 게시글 삭제
     */
    @Transactional
    public void postDelete(Integer postId) {
        postRepo.deleteById(postId);
    }

    /**
     * ✅ 전체 게시글 수 조회
     */
    public long getCount() {
        return postRepo.count();
    }

    /**
     * ✅ 찜 많은 순서대로 상위 4개 조회 (SOLD 제외)
     */
    public List<Posts> findTop4ByOrderByWishlistCountDesc() {
        Pageable pageable = PageRequest.of(0, 4);
        return postRepo.findTop4ByStatusNotOrderByWishlistCountDesc(pageable);
    }

    /**
     * ✅ 최신 순서대로 상위 12개 조회 (SOLD 제외)
     */
    public List<Posts> findTop12ByOrderByCreatedAtDesc() {
        Pageable pageable = PageRequest.of(0, 12);
        return postRepo.findTop12ByStatusNotOrderByCreatedAtDesc(pageable);
    }
}


