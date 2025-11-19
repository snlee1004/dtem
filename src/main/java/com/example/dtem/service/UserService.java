package com.example.dtem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dtem.dao.UserDAO;
import com.example.dtem.dto.UserDTO;
import com.example.dtem.entity.Users;

@Service
public class UserService {
	@Autowired
	UserDAO dao;

	// 로그인 처리 - 성공 시 사용자 이름 반환, 실패 시 null 반환
	public String login(String email, String password) {
		return dao.login(email, password);
	}
	
	// 회원가입 처리 - 저장 성공 시 1, 실패 시 0 반환
	public int userWrite(UserDTO dto) {
		return dao.userWrite(dto);
	}
	
	// 아이디(userId) 존재 여부 확인
	public boolean isExistId(Integer userId) {
		return dao.isExistId(userId);
	}
	
	// 이메일 중복 확인
	public boolean isExistEmail(String email) {
		return dao.isExistEmail(email);
	}
	
	// 전화번호 중복 확인
	public boolean isExistPhone(String phone) {
		return dao.isExistPhone(phone);
	}
	
	// 회원 정보 조회
	public UserDTO getUser(Integer userId) {
		Users user = dao.getUser(userId);
		if(user == null) return null;
		
		UserDTO dto = new UserDTO();
		dto.setUserId(user.getUserId());
		dto.setEmail(user.getEmail());
		dto.setPassword(user.getPassword());
		dto.setUsername(user.getUsername());
		dto.setPhone(user.getPhone());
		dto.setProfileImage(user.getProfileImage());
		dto.setAddr1(user.getAddr1());
		dto.setAddr2(user.getAddr2());
		dto.setAddr3(user.getAddr3());
		dto.setAddr4(user.getAddr4());
		dto.setIntroduce(user.getIntroduce());
		dto.setRating(user.getRating());
		dto.setReviewCount(user.getReviewCount());
		dto.setSellCount(user.getSellCount());
		dto.setBuyCount(user.getBuyCount());
		dto.setCreatedAt(user.getCreatedAt());
		dto.setUpdatedAt(user.getUpdatedAt());
		
		return dto;
	}
	
	// 회원정보 수정
	public int modify(UserDTO dto) {
		return dao.modify(dto);
	}
	
	// 이메일로 회원 정보 조회
	public UserDTO getUserByEmail(String email) {
		Users user = dao.getUserByEmail(email);
		if(user == null) return null;
		
		UserDTO dto = new UserDTO();
		dto.setUserId(user.getUserId());
		dto.setEmail(user.getEmail());
		dto.setPassword(user.getPassword());
		dto.setUsername(user.getUsername());
		dto.setPhone(user.getPhone());
		dto.setProfileImage(user.getProfileImage());
		dto.setAddr1(user.getAddr1());
		dto.setAddr2(user.getAddr2());
		dto.setAddr3(user.getAddr3());
		dto.setAddr4(user.getAddr4());
		dto.setIntroduce(user.getIntroduce());
		dto.setRating(user.getRating());
		dto.setReviewCount(user.getReviewCount());
		dto.setSellCount(user.getSellCount());
		dto.setBuyCount(user.getBuyCount());
		dto.setCreatedAt(user.getCreatedAt());
		dto.setUpdatedAt(user.getUpdatedAt());
		
		return dto;
	}
	
	// 회원 탈퇴
	public int deleteUser(Integer userId) {
		return dao.deleteUser(userId);
	}
	
	// 이름과 전화번호로 사용자 조회 (아이디 찾기)
	public UserDTO getUserByNameAndPhone(String username, String phone) {
		Users user = dao.getUserByNameAndPhone(username, phone);
		if(user == null) return null;
		
		UserDTO dto = new UserDTO();
		dto.setUserId(user.getUserId());
		dto.setEmail(user.getEmail());
		dto.setUsername(user.getUsername());
		dto.setPhone(user.getPhone());
		
		return dto;
	}
	
	// 이메일, 이름, 전화번호로 사용자 조회 (비밀번호 찾기)
	public UserDTO getUserByEmailAndNameAndPhone(String email, String username, String phone) {
		Users user = dao.getUserByEmailAndNameAndPhone(email, username, phone);
		if(user == null) return null;
		
		UserDTO dto = new UserDTO();
		dto.setUserId(user.getUserId());
		dto.setEmail(user.getEmail());
		dto.setUsername(user.getUsername());
		dto.setPassword(user.getPassword());
		dto.setPhone(user.getPhone());
		
		return dto;
	}
}