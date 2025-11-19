package com.example.dtem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.dtem.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer>{
	// 페이징 처리를 위한 조회
	@Query(value = "select * from "
			+ "(select rownum rn, tt.* from "
			+ "(select * from users order by userid desc) tt) "
			+ "where rn >= :startNum and rn <= :endNum", nativeQuery = true)
	List<Users> findByStartNumAndEndNum(@Param("startNum") int startNum, @Param("endNum") int endNum);
	
	// 로그인 처리 - 이메일과 비밀번호로 사용자 조회
	Users findByEmailAndPassword(String email, String password);
	
	// 이메일 중복 확인
	boolean existsByEmail(String email);
	
	// 전화번호 중복 확인
	boolean existsByPhone(String phone);
	
	// 이메일로 사용자 조회
	Users findByEmail(String email);
	
	// 이름과 전화번호로 사용자 조회 (아이디 찾기)
	// 여러 명이 있을 수 있으므로 첫 번째 결과만 반환
	Users findFirstByUsernameAndPhone(String username, String phone);
	
	// 이메일, 이름, 전화번호로 사용자 조회 (비밀번호 찾기)
	Users findByEmailAndUsernameAndPhone(String email, String username, String phone);
}


