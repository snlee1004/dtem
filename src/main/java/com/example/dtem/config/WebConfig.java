package com.example.dtem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 설정
 * - 정적 리소스 (이미지, CSS, JS 등) 경로 매핑
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 정적 리소스 핸들러 설정
     * /uploads/** 경로로 요청이 오면 static/uploads/ 폴더에서 파일 제공
     * - classpath: 컴파일된 리소스 (target/classes)
     * - file: 실제 파일 시스템 절대 경로 (개발 중 업로드된 파일)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로젝트 루트 기준 절대 경로 생성
        String projectPath = System.getProperty("user.dir");
        String uploadPath = "file:" + projectPath + File.separator + "src" + File.separator + 
                           "main" + File.separator + "resources" + File.separator + 
                           "static" + File.separator + "uploads" + File.separator;
        
        System.out.println("✅ Upload 경로 설정: " + uploadPath);
        
        // 프로필 이미지 및 게시물 이미지 경로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                    "classpath:/static/uploads/",
                    uploadPath
                );
        
        // 기타 정적 리소스 (선택사항)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}

