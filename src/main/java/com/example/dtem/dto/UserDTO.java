package com.example.dtem.dto;

import java.time.LocalDateTime;

import com.example.dtem.entity.Users;
import lombok.Data;

@Data
public class UserDTO {
    private Integer userId;
    private String email;
    private String password;
    private String username;
    private String phone;
    private String profileImage;
    private String addr1;                 
    private String addr2;                 
    private String addr3;                 
    private String addr4;                 
    private String introduce;
    private Double rating;
    private Integer reviewCount;
    private Integer sellCount;
    private Integer buyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Users toEntity() {
    	Users user = new Users();
    	user.setUserId(userId);
    	user.setEmail(email);
    	user.setPassword(password);
    	user.setUsername(username);
    	user.setPhone(phone);
    	user.setProfileImage(profileImage);
    	user.setAddr1(addr1);
    	user.setAddr2(addr2);
    	user.setAddr3(addr3);
    	user.setAddr4(addr4);
    	user.setIntroduce(introduce);
    	user.setRating(rating != null ? rating : 0.0);
    	user.setReviewCount(reviewCount != null ? reviewCount : 0);
    	user.setSellCount(sellCount != null ? sellCount : 0);
    	user.setBuyCount(buyCount != null ? buyCount : 0);
    	if(createdAt != null) {
    		user.setCreatedAt(createdAt);
    	}
    	if(updatedAt != null) {
    		user.setUpdatedAt(updatedAt);
    	}
    	return user;
    }
}


