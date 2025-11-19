package com.example.dtem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.dtem.entity.Posts;
import com.example.dtem.service.PostService;

@Controller
public class IndexController {
    
    @Autowired
    PostService service;
    
    @GetMapping("/index")
    public String index(Model model) {
        // 인기 찜상품 (wishlistCount 많은 순 4개)
        List<Posts> wishList = service.findTop4ByOrderByWishlistCountDesc();
        model.addAttribute("wishList", wishList);
        
        // 최근 등록된 상품 (createdAt 최신순 12개)
        List<Posts> latestList = service.findTop12ByOrderByCreatedAtDesc();
        model.addAttribute("latestList", latestList);
        
        return "/main/index";
    }
}


