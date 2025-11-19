package com.example.dtem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${mail.from.address:noreply@onultem.com}")
	private String fromAddress;
	
	@Value("${mail.from.name:오늘득템}")
	private String fromName;
	
	// 랜덤 6자리 인증번호 생성
	public String generateVerificationCode() {
		Random random = new Random();
		int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
		return String.valueOf(code);
	}
	
	// 인증번호 이메일 발송 (타입별로 제목과 내용 다르게 설정)
	public boolean sendVerificationEmail(String toEmail, String verificationCode, String type) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(toEmail);
		
		// 타입에 따라 제목과 내용 다르게 설정
		if ("signup".equals(type)) {
			// 회원가입 인증번호
			message.setSubject("[오늘득템] 회원가입 인증번호");
			message.setText(
				"안녕하세요, 오늘득템입니다.\n\n" +
				"회원가입을 위한 인증번호는 다음과 같습니다.\n\n" +
				"인증번호: " + verificationCode + "\n\n" +
				"인증번호를 입력하시면 회원가입을 완료하실 수 있습니다.\n" +
				"본인이 요청하지 않은 경우 이 이메일을 무시하셔도 됩니다.\n\n" +
				"감사합니다.\n" +
				"오늘득템 드림"
			);
		} else if ("findPassword".equals(type)) {
			// 비밀번호 찾기 인증번호
			message.setSubject("[오늘득템] 비밀번호 찾기 인증번호");
			message.setText(
				"안녕하세요, 오늘득템입니다.\n\n" +
				"비밀번호 찾기를 위한 인증번호는 다음과 같습니다.\n\n" +
				"인증번호: " + verificationCode + "\n\n" +
				"인증번호를 입력하시면 비밀번호를 확인하실 수 있습니다.\n" +
				"본인이 요청하지 않은 경우 이 이메일을 무시하셔도 됩니다.\n\n" +
				"감사합니다.\n" +
				"오늘득템 드림"
			);
		} else {
			// 기본값 (비밀번호 찾기)
			message.setSubject("[오늘득템] 인증번호");
			message.setText(
				"안녕하세요, 오늘득템입니다.\n\n" +
				"인증번호: " + verificationCode + "\n\n" +
				"감사합니다.\n" +
				"오늘득템 드림"
			);
		}
		
		mailSender.send(message);
		return true;
	}
}

