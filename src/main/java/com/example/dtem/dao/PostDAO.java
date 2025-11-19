package com.example.dtem.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.dtem.entity.Posts;
import com.example.dtem.repository.PostRepository;

@Repository
@Transactional
public class PostDAO {

    @Autowired
    private PostRepository postRepository;

    // ===========================
    // 게시글 관련
    // ===========================

    /** 전체 게시글 수 조회 */
    public long getCount() {
        return postRepository.count();
    }

    /** 게시글 목록 조회 */
    public List<Posts> postList() {
        return postRepository.findAll();
    }

    /** 게시글 상세 조회 */
    public Posts postView(Integer postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        // 조회수 증가
        post.setHit(post.getHit() + 1);
        postRepository.save(post);
        return post;
    }

    /** 게시글 등록 */
    public Posts postWrite(Posts post) {
        return postRepository.save(post);
    }

    /** 게시글 수정 */
    public Posts postModify(Integer postId, Posts modifiedPost) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setTitle(modifiedPost.getTitle());
        post.setContent(modifiedPost.getContent());
        post.setPrice(modifiedPost.getPrice());
        post.setCategory(modifiedPost.getCategory());
        post.setLocation(modifiedPost.getLocation());
        post.setConditionStatus(modifiedPost.getConditionStatus());
        post.setTradeMethod(modifiedPost.getTradeMethod());
        post.setNegotiable(modifiedPost.getNegotiable());

        return postRepository.save(post);
    }

    /** 게시글 삭제 */
    public boolean postDelete(Integer postId) {
        if (!postRepository.existsById(postId)) {
            return false;
        }
        postRepository.deleteById(postId);
        return true;
    }

    /** 카테고리별 게시글 조회 */
    public List<Posts> getPostsByCategory(String category) {
        return postRepository.findByCategory(category);
    }

    /** 회원별 게시글 조회 */
    public List<Posts> getPostsByUserId(int userId) {
        return postRepository.findByUserId(userId);
    }

    /** 판매중 게시글 조회 */
    public List<Posts> getAvailablePosts() {
        return postRepository.findByStatus("SELLING");
    }

    /** 제목 검색 */
    public List<Posts> searchByTitle(String keyword) {
        return postRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /** 판매 완료 */
    public void markAsSold(Integer postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.setStatus("SOLD");
        postRepository.save(post);
    }

    /** 판매중 변경 */
    public void markAsAvailable(Integer postId) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.setStatus("SELLING");
        postRepository.save(post);
    }
}
