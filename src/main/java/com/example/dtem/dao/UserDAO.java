package com.example.dtem.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.dtem.dto.UserDTO;
import com.example.dtem.entity.Users;
import com.example.dtem.repository.UserRepository;

@Repository
public class UserDAO {
	@Autowired
	UserRepository userRepository;

	// 로그인 처리 - 이메일과 비밀번호로 사용자 인증
	// 성공 시 사용자 이름 반환, 실패 시 null 반환
	public String login(String email, String password) {
		Users user = userRepository.findByEmailAndPassword(email, password);
		if(user != null) return user.getUsername();
		return null;
	}
	
	// 회원가입 처리 - 새로운 회원 정보를 DB에 저장
	// 반환값: 1=성공, 0=실패, -1=이메일 중복
	public int userWrite(UserDTO dto) {
		// 중복 이메일 체크
		if(userRepository.existsByEmail(dto.getEmail())) {
			return -1;  // 이메일 중복
		}
		
		// 신규 가입 시 userId, createdAt, updatedAt은 null로 설정 (자동 생성/설정됨)
		dto.setUserId(null);
		dto.setCreatedAt(null);
		dto.setUpdatedAt(null);
		
		// 새 회원 엔티티 생성 및 저장
		Users user = userRepository.save(dto.toEntity());
		return (user != null && user.getUserId() != null) ? 1 : 0;
	}
	
	// 이메일 중복 확인
	public boolean isExistEmail(String email) {
		return userRepository.existsByEmail(email);
	}
	
	// 전화번호 중복 확인
	public boolean isExistPhone(String phone) {
		return userRepository.existsByPhone(phone);
	}
	
	// 아이디 존재 여부 확인
	// 존재하면 true, 존재하지 않으면 false 반환
	public boolean isExistId(Integer userId) {
		return userRepository.existsById(userId);
	}
	
	// 회원 정보 조회 - userId로 회원 정보 가져오기
	public Users getUser(Integer userId) {
		return userRepository.findById(userId).orElse(null);
	}
	
	// 회원정보 수정 처리
	// 성공 시 1, 실패 시 0 반환
	public int modify(UserDTO dto) {
		Users existingUser = userRepository.findById(dto.getUserId()).orElse(null);
		
		if(existingUser == null) {
			return 0;
		}
		
		// 수정 시 createdAt은 기존값 유지, updatedAt은 자동 갱신
		dto.setCreatedAt(existingUser.getCreatedAt());
		dto.setUpdatedAt(null);  // null로 설정하면 @UpdateTimestamp가 자동 갱신
		
		Users updatedUser = userRepository.save(dto.toEntity());
		return (updatedUser != null) ? 1 : 0;
	}
	
	// 이메일로 회원 정보 조회
	public Users getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	// 회원 탈퇴 처리
	// 성공 시 1, 실패 시 0 반환
	public int deleteUser(Integer userId) {
		if(!userRepository.existsById(userId)) {
			return 0;
		}
		userRepository.deleteById(userId);
		return 1;
	}
	
	// 이름과 전화번호로 사용자 조회 (아이디 찾기)
	// 여러 명이 있을 경우 첫 번째 결과만 반환
	public Users getUserByNameAndPhone(String username, String phone) {
		return userRepository.findFirstByUsernameAndPhone(username, phone);
	}
	
	// 이메일, 이름, 전화번호로 사용자 조회 (비밀번호 찾기)
	public Users getUserByEmailAndNameAndPhone(String email, String username, String phone) {
		return userRepository.findByEmailAndUsernameAndPhone(email, username, phone);
	}
}