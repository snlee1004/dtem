package com.example.dtem.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.dtem.entity.PostLike;
import com.example.dtem.entity.Posts;
import com.example.dtem.entity.Users;
import com.example.dtem.repository.PostLikeRepository;
import com.example.dtem.repository.PostRepository;
import com.example.dtem.repository.UserRepository;
import com.example.dtem.service.PostService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PostLikeRepository postLikeRepository;
    
    @Autowired
    private com.example.dtem.service.ReviewService reviewService; 

    @Value("${file.upload.dir:src/main/resources/static/uploads/posts}")
    private String uploadDir;
    
    /**
     * 업로드 디렉토리를 절대 경로로 초기화
     */
    @PostConstruct
    public void init() {
        // 상대 경로를 절대 경로로 변환
        if (!uploadDir.startsWith("/") && !uploadDir.contains(":")) {
            String projectPath = System.getProperty("user.dir");
            uploadDir = projectPath + File.separator + uploadDir.replace("/", File.separator);
        }
        System.out.println("✅ 파일 업로드 경로: " + uploadDir);
    }

    private static final int PAGE_SIZE = 5;   // 한 페이지당 글 수
    private static final int PAGE_BLOCK = 10;  // 페이징 블록 크기 (1~10)

    /**
     * ✅ 게시글 목록 조회 (페이징 + 동적 검색)
     * 
     * @param pg 페이지 번호
     * @param sort 정렬 방식 (wishlist: 찜 많은 순, latest: 최신순)
     * @param title 제목 검색어
     * @param sido 시/도
     * @param sigungu 시/군/구
     * @param dong 읍/면/동
     * @param category 카테고리
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     */
    @GetMapping("/postList")
    public String postList(
            Model model, 
            @RequestParam(name = "pg", defaultValue = "1") int pg,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "sido", required = false) String sido,
            @RequestParam(name = "sigungu", required = false) String sigungu,
            @RequestParam(name = "dong", required = false) String dong,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "minPrice", required = false) Integer minPrice,
            @RequestParam(name = "maxPrice", required = false) Integer maxPrice) {

        int pageIndex = pg - 1;
        
        // 정렬 방식 결정
        Sort sortOption;
        if ("wishlist".equals(sort)) {
            // 찜 많은 순 (wishlistCount 내림차순)
            sortOption = Sort.by(Sort.Direction.DESC, "wishlistCount");
        } else {
            // 최신순 (createdAt 내림차순) - 기본값
            sortOption = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE, sortOption);
        
        // 동적 검색 조건 생성
        org.springframework.data.jpa.domain.Specification<Posts> spec = 
            com.example.dtem.repository.PostSpecification.searchPosts(
                title, sido, sigungu, dong, category, minPrice, maxPrice);
        
        // 검색 실행
        Page<Posts> page = postRepository.findAll(spec, pageable);
        
        // 디버깅 로그
        System.out.println("==== PostList 디버깅 ====");
        System.out.println("검색 파라미터 - title: " + title + ", sido: " + sido + ", sigungu: " + sigungu + ", dong: " + dong);
        System.out.println("검색 파라미터 - category: " + category + ", minPrice: " + minPrice + ", maxPrice: " + maxPrice);
        System.out.println("총 게시물 수: " + page.getTotalElements());
        System.out.println("현재 페이지 게시물 수: " + page.getContent().size());
        System.out.println("====================");

        int totalPages = page.getTotalPages();
        if (totalPages == 0) totalPages = 1; // 최소 1페이지 보장
        int startPage = (pg - 1) / PAGE_BLOCK * PAGE_BLOCK + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK - 1, totalPages);

        // 검색 파라미터를 뷰에 전달 (검색 조건 유지용)
        model.addAttribute("pg", pg);
        model.addAttribute("sort", sort);
        model.addAttribute("title", title);
        model.addAttribute("sido", sido);
        model.addAttribute("sigungu", sigungu);
        model.addAttribute("dong", dong);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        
        model.addAttribute("posts", page.getContent());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", pg);

        return "post/postList"; // ✅ templates/post/postList.html
    }

    /**
     * ✅ 게시글 상세 조회 (조회수 증가 + 판매자 정보 + 찜 여부)
     */
    @GetMapping("/postView/{postId}")
    public String postView(@PathVariable("postId") Integer postId, Model model, HttpSession session) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 조회수 증가
        post.setHit(post.getHit() + 1);
        postRepository.save(post);
        
        // 판매자 정보 조회
        Users seller = userRepository.findById(post.getUserId())
                .orElse(null);
        
        // 판매자의 평균 평점 조회
        Double sellerRating = null;
        if (seller != null) {
            sellerRating = reviewService.getAverageRating(String.valueOf(seller.getUserId()));
        }
        
        // 현재 사용자의 찜 여부 확인
        Integer currentUserId = (Integer) session.getAttribute("userId");
        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = postLikeRepository.existsByPostIdAndUserId(postId, currentUserId);
        }

        model.addAttribute("post", post);
        model.addAttribute("seller", seller);
        model.addAttribute("sellerRating", sellerRating);
        model.addAttribute("isLiked", isLiked);
        return "post/postView";
    }
    
    /**
     * ✅ 찜하기 토글 (추가/취소)
     */
    @PostMapping("/{postId}/like")
    @ResponseBody
    public Map<String, Object> toggleLike(@PathVariable("postId") Integer postId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 로그인 체크
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return result;
            }
            
            // 게시물 조회
            Posts post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
            
            // 이미 찜했는지 확인
            Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
            
            boolean liked;
            if (existingLike.isPresent()) {
                // 찜 취소
                postLikeRepository.delete(existingLike.get());
                post.setWishlistCount(Math.max(0, post.getWishlistCount() - 1));
                liked = false;
            } else {
                // 찜 추가
                PostLike newLike = new PostLike();
                newLike.setPostId(postId);
                newLike.setUserId(userId);
                postLikeRepository.save(newLike);
                post.setWishlistCount(post.getWishlistCount() + 1);
                liked = true;
            }
            
            postRepository.save(post);
            
            result.put("success", true);
            result.put("liked", liked);
            result.put("wishlistCount", post.getWishlistCount());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }


    /**
     * ✅ 게시글 작성 폼
     */
    @GetMapping("/postWriteForm")
    public String postWriteForm() {
        return "post/postWriteForm";
    }

    /**
     * ✅ 게시글 작성 처리
     */
    @PostMapping("/postWrite")
    public String postWrite(
            @ModelAttribute Posts post, 
            @RequestParam(value = "productImages", required = false) List<MultipartFile> productImages,
            HttpSession session,
            Model model) {
        
        // 세션에서 로그인한 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            // 로그인하지 않은 경우
            return "redirect:/user/loginForm";
        }
        
        // 이미지 파일 업로드 처리
        if (productImages != null && !productImages.isEmpty() && !productImages.get(0).isEmpty()) {
            String allImagePaths = savePostImages(productImages);
            post.setPostImage(allImagePaths); // 모든 이미지 경로를 쉼표로 구분하여 저장
        }
        
        // 게시글 정보 설정
        post.setUserId(userId);
        post.setCreatedAt(LocalDateTime.now());
        post.setStatus("SELLING"); // 판매중으로 기본 설정
        
        // negotiable 기본값 설정 (체크 안했으면 N)
        if (post.getNegotiable() == null || post.getNegotiable().isEmpty()) {
            post.setNegotiable("N");
        }
        
        postRepository.save(post);
        
        // 등록 성공 메시지와 함께 리다이렉트
        model.addAttribute("message", "등록되었습니다");
        return "redirect:/post/postList?registered=true";
    }
    
    /**
     * ✅ 게시글 이미지 저장 (여러 파일)
     * 여러 이미지를 저장하고, 경로를 쉼표(,)로 구분하여 반환
     */
    private String savePostImages(List<MultipartFile> files) {
        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            System.out.println("📁 업로드 디렉토리: " + uploadPath.toAbsolutePath());
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ 디렉토리 생성 완료: " + uploadPath.toAbsolutePath());
            }
            
            StringBuilder imagePathsBuilder = new StringBuilder();
            
            // 각 파일 저장
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                
                // 파일명 생성 (UUID + 원본 확장자)
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + extension;
                
                // 파일 저장
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("💾 파일 저장 완료: " + filePath.toAbsolutePath());
                System.out.println("🌐 웹 경로: /uploads/posts/" + uniqueFilename);
                
                // 이미지 경로 추가 (쉼표로 구분)
                if (imagePathsBuilder.length() > 0) {
                    imagePathsBuilder.append(",");
                }
                imagePathsBuilder.append("/uploads/posts/").append(uniqueFilename);
            }
            
            String result = imagePathsBuilder.toString();
            System.out.println("📋 DB에 저장될 경로: " + result);
            
            // 쉼표로 구분된 이미지 경로 반환
            return result;
            
        } catch (IOException e) {
            System.err.println("❌ 파일 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ 게시글 수정 폼
     */
    @GetMapping("/postModifyForm/{postId}")
    public String postModifyForm(@PathVariable("postId") Integer postId, Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/user/loginForm";
        }
        
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 본인 글이 아니면 수정 불가
        if (!Objects.equals(post.getUserId(), userId)) {
            return "redirect:/post/postView/" + postId;
        }
        
        model.addAttribute("post", post);
        return "post/postModifyForm";
    }
    
    /**
     * ✅ 게시글 수정 처리
     */
    @PostMapping("/postModify/{postId}")
    public String postModify(
            @PathVariable("postId") Integer postId,
            @ModelAttribute Posts updatedPost,
            @RequestParam(value = "productImages", required = false) List<MultipartFile> productImages,
            @RequestParam(value = "existingImages", required = false) String existingImages,
            HttpSession session,
            Model model) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/user/loginForm";
        }
        
        Posts existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        // 본인 글이 아니면 수정 불가
        if (!Objects.equals(existingPost.getUserId(), userId)) {
            return "redirect:/post/postView/" + postId;
        }
        
        // 이미지 처리: 기존 이미지 + 새 이미지
        StringBuilder finalImagePaths = new StringBuilder();
        
        // 1. 기존 이미지 추가
        if (existingImages != null && !existingImages.trim().isEmpty()) {
            finalImagePaths.append(existingImages);
        }
        
        // 2. 새 이미지 업로드 및 추가
        if (productImages != null && !productImages.isEmpty()) {
            String newImagePaths = savePostImages(productImages);
            if (newImagePaths != null && !newImagePaths.isEmpty()) {
                if (finalImagePaths.length() > 0) {
                    finalImagePaths.append(",");
                }
                finalImagePaths.append(newImagePaths);
            }
        }
        
        // 이미지 경로 업데이트
        existingPost.setPostImage(finalImagePaths.toString());
        
        // 기본 정보 업데이트
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setPrice(updatedPost.getPrice());
        existingPost.setCategory(updatedPost.getCategory());
        existingPost.setLocation(updatedPost.getLocation());
        existingPost.setConditionStatus(updatedPost.getConditionStatus());
        existingPost.setTradeMethod(updatedPost.getTradeMethod());
        existingPost.setNegotiable(updatedPost.getNegotiable());
        // status는 수정 시 변경하지 않음 (판매상태는 별도로 관리)
        existingPost.setUpdatedAt(LocalDateTime.now());
        
        postRepository.save(existingPost);
        
        return "redirect:/post/postView/" + postId;
    }
    
    /**
     * ✅ 게시글 삭제
     */
    @PostMapping("/postDelete/{postId}")
    public String postDelete(@PathVariable("postId") Integer postId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/user/loginForm";
        }
        
        Posts post = postRepository.findById(postId).orElse(null);
        
        // 본인 글이 아니면 삭제 불가
        if (post != null && Objects.equals(post.getUserId(), userId)) {
            postRepository.deleteById(postId);
        }
        
        return "redirect:/post/postList";
    }
}
