package com.example.dtem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.dtem.dto.UserDTO;
import com.example.dtem.dto.ChatRoomDTO;
import com.example.dtem.entity.Posts;
import com.example.dtem.entity.Users;
import com.example.dtem.repository.PostRepository;
import com.example.dtem.repository.UserRepository;
import com.example.dtem.service.EmailService;
import com.example.dtem.service.UserService;
import com.example.dtem.service.ChatRoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class UserController {
	
	@Autowired
	UserService service;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	PostRepository postRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ChatRoomService chatRoomService;
	
	@Autowired
	com.example.dtem.service.ReviewService reviewService;
	
	@Autowired
	com.example.dtem.service.TradeService tradeService;
	
	@Autowired
	com.example.dtem.repository.PostLikeRepository postLikeRepository;
	
	@Autowired
	com.example.dtem.repository.TradeRepository tradeRepository;
	
	@Autowired
	com.example.dtem.repository.ChatRoomRepository chatRoomRepository;
	
	@Autowired
	com.example.dtem.repository.ReviewRepository reviewRepository;
	
	@Value("${file.upload.profile.dir:src/main/resources/static/uploads/profiles}")
	private String uploadDir;
	
	// 로그인 폼 페이지
		@GetMapping("/user/loginForm")
		public String loginForm() {
			return "/user/loginForm";
		}
		
	// 로그인 처리
	@PostMapping("/user/login")
	public String login(HttpServletRequest request, 
			HttpSession session) {
		String id = request.getParameter("id"); // loginForm의 input name과 일치
		String pwd = request.getParameter("pwd");
		
		String username = service.login(id, pwd);
		
		if(username != null) {
			// 로그인 성공 시 userId도 세션에 저장
			UserDTO user = service.getUserByEmail(id);
			session.setAttribute("userId", user.getUserId());
			session.setAttribute("userName", username);
			session.setAttribute("userEmail", id);
			return "/user/loginOK";
		} else {
			return "/user/loginFail";
		}
	}
		// 로그아웃 처리
		@GetMapping("/user/logout")
		public String logout(HttpSession session) {
			session.invalidate(); // 세션 초기화
			return "/user/logout";
		}
		
		// 회원가입 폼 페이지
		@GetMapping("/user/userWriteForm")
		public String userWriteForm() {
			return "/user/userWriteForm";
		}
		// 회원가입 처리
		@PostMapping("/user/userWrite")
		public String userWrite(UserDTO dto, Model model) {
			// createdAt, updatedAt은 JPA의 @CreationTimestamp, @UpdateTimestamp가 자동 처리
			// 수동으로 설정하지 않음
			int result = service.userWrite(dto);
			
			model.addAttribute("result", result);
			return "/user/write";  
		}
		
		// 아이디 중복 체크
		@GetMapping("/user/checkId")
		public String checkId(HttpServletRequest request, Model model) {
			String str_userId = request.getParameter("userId");
			Integer userId = null;
			if(str_userId != null && !str_userId.trim().isEmpty()) {
				userId = Integer.parseInt(str_userId);
			}
			boolean isExist = (userId != null) ? service.isExistId(userId) : false;
			model.addAttribute("isExist", isExist);
			return "/user/checkId";
		}
		
		// 회원정보 수정 폼
		@GetMapping("/user/modifyForm")
		public String modifyForm(HttpServletRequest request, Model model) {
	/*	String str_userId = request.getParameter("userId");
			Integer userId = null;
			if(str_userId != null && !str_userId.trim().isEmpty()) {
				userId = Integer.parseInt(str_userId);
			}
			UserDTO dto = (userId != null) ? service.getUser(userId) : null;
			model.addAttribute("dto", dto);
	*/
			return "/user/modifyForm";
		}
		
		// 회원정보 수정 처리
		@PostMapping("/user/modify")
		public String modify(UserDTO dto, Model model) {
			int result = service.modify(dto);
			model.addAttribute("result", result);
			return "/user/modify";
		}
		
	// 마이페이지 폼
	@GetMapping("/user/myPage")
	public String myPage(@RequestParam(name = "wishlistPg", defaultValue = "1") int wishlistPg,
	                     HttpSession session, 
	                     Model model) {
		// 세션에서 로그인한 사용자 이메일 가져오기
		String userEmail = (String) session.getAttribute("userEmail");
		Integer userId = (Integer) session.getAttribute("userId");
		
		// 로그인하지 않은 경우 로그인 페이지로 이동
		if(userEmail == null || userEmail.trim().isEmpty()) {
			return "redirect:/user/loginForm";
		}
		
		// 이메일로 사용자 정보 조회
		UserDTO dto = service.getUserByEmail(userEmail);
		if(dto != null) {
			model.addAttribute("user", dto);
		}
		
		// 채팅방 목록 가져오기
		if(userId != null) {
			String userIdStr = String.valueOf(userId);
			
			List<ChatRoomDTO> allChatRooms = chatRoomService.getUserChatRooms(userIdStr);
			
			// 구매 채팅과 판매 채팅 분리
			List<ChatRoomDTO> buyChatRooms = new java.util.ArrayList<>();
			List<ChatRoomDTO> sellChatRooms = new java.util.ArrayList<>();
			
			for(ChatRoomDTO room : allChatRooms) {
				if(room.isSeller()) {
					sellChatRooms.add(room);
				} else {
					buyChatRooms.add(room);
				}
			}
			
			model.addAttribute("buyChatRooms", buyChatRooms);
			model.addAttribute("sellChatRooms", sellChatRooms);
			
			// 리뷰 정보 가져오기
			List<com.example.dtem.dto.ReviewWithUserDTO> receivedReviews = reviewService.getReceivedReviewsWithUser(userIdStr);
			Double averageRating = reviewService.getAverageRating(userIdStr);
			Long reviewCount = reviewService.getReviewCount(userIdStr);
			
			// 거래 횟수 가져오기
			Long sellCount = tradeService.countCompletedSales(userIdStr);
			Long buyCount = tradeService.countCompletedPurchases(userIdStr);
			
			model.addAttribute("receivedReviews", receivedReviews);
			model.addAttribute("averageRating", averageRating);
			model.addAttribute("reviewCount", reviewCount);
			model.addAttribute("sellCount", sellCount);
			model.addAttribute("buyCount", buyCount);
			
		// 찜 목록 가져오기 (페이징 - 4개씩)
		Pageable pageable = PageRequest.of(wishlistPg - 1, 4, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<com.example.dtem.entity.PostLike> wishlistPage = postLikeRepository.findByUserIdWithPost(userId, pageable);
		
		// 페이징 정보 계산 (1-5 블록)
		int totalPages = wishlistPage.getTotalPages();
		if (totalPages == 0) totalPages = 1;
		int startPage = (wishlistPg - 1) / 5 * 5 + 1;
		int endPage = Math.min(startPage + 4, totalPages);
		
		model.addAttribute("wishlistItems", wishlistPage.getContent());
		model.addAttribute("wishlistTotalPages", totalPages);
		model.addAttribute("wishlistStartPage", startPage);
		model.addAttribute("wishlistEndPage", endPage);
		model.addAttribute("wishlistCurrentPage", wishlistPg);
		
		// 판매 내역 가져오기 (판매중 먼저, 그 다음 판매완료, 각각 최신순)
		List<com.example.dtem.entity.Posts> sellHistory = postRepository.findByUserId(userId);
		
		// 세션에서 숨긴 판매 내역 가져오기
		@SuppressWarnings("unchecked")
		List<Integer> hiddenSellPosts = (List<Integer>) session.getAttribute("hiddenSellPosts");
		if (hiddenSellPosts != null) {
			// 숨긴 항목 제외
			sellHistory.removeIf(post -> hiddenSellPosts.contains(post.getPostId()));
		}
		
		sellHistory.sort((a, b) -> {
			// null-safe status 비교
			String statusA = a.getStatus() != null ? a.getStatus() : "";
			String statusB = b.getStatus() != null ? b.getStatus() : "";
			
			// 상태별 우선순위: SELLING(0) > SOLD(1) > 기타(2)
			int statusCompare = (statusA.equals("SELLING") ? 0 : statusA.equals("SOLD") ? 1 : 2) - 
			                    (statusB.equals("SELLING") ? 0 : statusB.equals("SOLD") ? 1 : 2);
			if (statusCompare != 0) return statusCompare;
			// 같은 상태면 최신순
			return b.getCreatedAt().compareTo(a.getCreatedAt());
		});
		model.addAttribute("sellHistory", sellHistory);
		
		// 구매 내역 가져오기 (Trade와 Posts 정보 매핑)
		List<com.example.dtem.entity.Trade> buyTrades = tradeRepository.findByBuyerIdOrderByCreatedAtDesc(userIdStr);
		List<Map<String, Object>> buyHistory = new java.util.ArrayList<>();
		
		// 세션에서 숨긴 구매 내역 가져오기
		@SuppressWarnings("unchecked")
		List<Long> hiddenBuyTrades = (List<Long>) session.getAttribute("hiddenBuyTrades");
		
		for (com.example.dtem.entity.Trade trade : buyTrades) {
			// 숨긴 항목 제외
			if (hiddenBuyTrades != null && hiddenBuyTrades.contains(trade.getTradeId())) {
				continue;
			}
			
			// 구매자(현재 사용자)가 채팅방을 나간 경우 제외
			if (trade.getRoomId() != null) {
				Optional<com.example.dtem.entity.ChatRoom> roomOpt = chatRoomRepository.findByRoomId(trade.getRoomId());
				if (roomOpt.isPresent()) {
					com.example.dtem.entity.ChatRoom room = roomOpt.get();
					// user2(구매자)가 나간 경우 제외
					if (room.getUser2Id().equals(userIdStr) && Boolean.TRUE.equals(room.getUser2Left())) {
						continue;
					}
				}
			}
			
			// Trade의 postId는 Long, Posts의 postId는 Integer이므로 변환 필요
			Optional<com.example.dtem.entity.Posts> postOpt = postRepository.findById(trade.getPostId().intValue());
			if (postOpt.isPresent()) {
				Map<String, Object> item = new java.util.HashMap<>();
				item.put("trade", trade);
				item.put("post", postOpt.get());
				
				// 리뷰 작성 여부 확인 (현재 사용자가 이 거래에 대해 리뷰를 작성했는지)
				boolean hasReview = reviewRepository.findByTradeIdAndReviewerId(trade.getTradeId(), userIdStr).isPresent();
				item.put("hasReview", hasReview);
				
				buyHistory.add(item);
			}
		}
		
		// 구매 내역 정렬 (거래중 먼저, 그 다음 구매완료, 각각 최신순)
		buyHistory.sort((a, b) -> {
			com.example.dtem.entity.Trade tradeA = (com.example.dtem.entity.Trade) a.get("trade");
			com.example.dtem.entity.Trade tradeB = (com.example.dtem.entity.Trade) b.get("trade");
			
			// 상태별 우선순위: IN_PROGRESS(0) > COMPLETED(1)
			int statusCompare = (tradeA.getStatus() == com.example.dtem.entity.Trade.TradeStatus.IN_PROGRESS ? 0 : 1) - 
			                    (tradeB.getStatus() == com.example.dtem.entity.Trade.TradeStatus.IN_PROGRESS ? 0 : 1);
			if (statusCompare != 0) return statusCompare;
			// 같은 상태면 최신순
			return tradeB.getCreatedAt().compareTo(tradeA.getCreatedAt());
		});
		
		model.addAttribute("buyHistory", buyHistory);
		
		// 대시보드용 통계 데이터
		model.addAttribute("sellHistoryCount", sellHistory.size());
		model.addAttribute("buyHistoryCount", buyHistory.size());
		model.addAttribute("wishlistCount", wishlistPage.getTotalElements());
		model.addAttribute("chatRoomCount", buyChatRooms.size() + sellChatRooms.size());
	}
		
		return "/user/myPage";
	}
		
		// 마이페이지
		@GetMapping("/user/myPageOk")
		public String myPageOk() {
			return "/user/myPageOk";
		}
		
	// 회원정보 수정 처리 - 마이페이지용
	@PostMapping("/user/updateProfile")
	public String updateProfile(HttpServletRequest request, 
	                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
	                            HttpSession session, 
	                            Model model) {
		// 세션에서 로그인한 사용자 이메일 가져오기
		String userEmail = (String) session.getAttribute("userEmail");
		
		// 로그인하지 않은 경우
		if(userEmail == null || userEmail.trim().isEmpty()) {
			return "redirect:/user/loginForm";
		}
		
		// 기존 사용자 정보 조회
		UserDTO existingUser = service.getUserByEmail(userEmail);
		if(existingUser == null) {
			return "redirect:/user/loginForm";
		}
		
		// 현재 비밀번호 확인
		String currentPassword = request.getParameter("currentPassword");
		if(currentPassword == null || !currentPassword.equals(existingUser.getPassword())) {
			model.addAttribute("user", existingUser);
			model.addAttribute("result", 0);
			model.addAttribute("pwdError", "비밀번호가 일치하지 않습니다.");
			model.addAttribute("activeSection", "edit-profile"); // 활성 섹션 지정
			return "/user/myPage";
		}
		
		// 폼에서 받은 데이터로 DTO 생성
		UserDTO dto = new UserDTO();
		dto.setUserId(existingUser.getUserId());
		dto.setEmail(existingUser.getEmail()); // 이메일은 변경 불가
		dto.setPassword(existingUser.getPassword()); // 비밀번호는 별도 변경 메뉴에서
		
		// 프로필 이미지 처리
		String profileImagePath = existingUser.getProfileImage(); // 기본은 기존 이미지 유지
		if(profileImage != null && !profileImage.isEmpty()) {
			// 새 이미지가 업로드된 경우
			String savedPath = saveProfileImage(profileImage);
			if(savedPath != null) {
				profileImagePath = savedPath;
			}
		}
		dto.setProfileImage(profileImagePath);
		
		// 나머지 정보 업데이트
		dto.setUsername(request.getParameter("username"));
		// 전화번호 포맷팅 적용
		String phone = request.getParameter("phone");
		dto.setPhone(formatPhoneNumber(phone));
		dto.setAddr1(request.getParameter("zipcode"));
		dto.setAddr2(request.getParameter("addrRoad"));
		dto.setAddr3(request.getParameter("addrJibun"));
		dto.setAddr4(request.getParameter("addrDetail"));
		dto.setIntroduce(request.getParameter("introduce"));
		
		// 기존 통계 정보 유지
		dto.setRating(existingUser.getRating());
		dto.setReviewCount(existingUser.getReviewCount());
		dto.setSellCount(existingUser.getSellCount());
		dto.setBuyCount(existingUser.getBuyCount());
			
		// 회원정보 수정 실행
		int result = service.modify(dto);
		
		if(result == 1) {
			// 세션의 사용자명 업데이트
			session.setAttribute("userName", dto.getUsername());
		}
		
		// 수정된 정보 다시 조회
		UserDTO updatedUser = service.getUserByEmail(userEmail);
		model.addAttribute("user", updatedUser);
		model.addAttribute("result", result);
		model.addAttribute("updateSuccess", result == 1);
		model.addAttribute("activeSection", "edit-profile"); // 활성 섹션 지정
		
		return "/user/myPage";
	}
	
	// 비밀번호 변경 처리
	@PostMapping("/user/changePassword")
	public String changePassword(HttpServletRequest request, HttpSession session, Model model) {
		// 세션에서 로그인한 사용자 이메일 가져오기
		String userEmail = (String) session.getAttribute("userEmail");
		
		// 로그인하지 않은 경우
		if(userEmail == null || userEmail.trim().isEmpty()) {
			return "redirect:/user/loginForm";
		}
		
		// 기존 사용자 정보 조회
		UserDTO existingUser = service.getUserByEmail(userEmail);
		if(existingUser == null) {
			return "redirect:/user/loginForm";
		}
		
		// 파라미터 받기
		String currentPassword = request.getParameter("currentPassword");
		String newPassword = request.getParameter("newPassword");
		
		// 현재 비밀번호 확인
		if(currentPassword == null || !currentPassword.equals(existingUser.getPassword())) {
			model.addAttribute("user", existingUser);
			model.addAttribute("result", 0);
			model.addAttribute("pwdChangeError", "현재 비밀번호가 일치하지 않습니다.");
			model.addAttribute("activeSection", "change-password"); // 활성 섹션 지정
			return "/user/myPage";
		}
		
		// 새 비밀번호 유효성 검증
		if(newPassword == null || newPassword.length() < 8) {
			model.addAttribute("user", existingUser);
			model.addAttribute("result", 0);
			model.addAttribute("pwdChangeError", "새 비밀번호는 8자 이상이어야 합니다.");
			model.addAttribute("activeSection", "change-password"); // 활성 섹션 지정
			return "/user/myPage";
		}
		
		// DTO 생성 (비밀번호만 변경)
		UserDTO dto = new UserDTO();
		dto.setUserId(existingUser.getUserId());
		dto.setEmail(existingUser.getEmail());
		dto.setPassword(newPassword); // 새 비밀번호
		dto.setUsername(existingUser.getUsername());
		dto.setPhone(existingUser.getPhone());
		dto.setProfileImage(existingUser.getProfileImage());
		dto.setAddr1(existingUser.getAddr1());
		dto.setAddr2(existingUser.getAddr2());
		dto.setAddr3(existingUser.getAddr3());
		dto.setAddr4(existingUser.getAddr4());
		dto.setIntroduce(existingUser.getIntroduce());
		dto.setRating(existingUser.getRating());
		dto.setReviewCount(existingUser.getReviewCount());
		dto.setSellCount(existingUser.getSellCount());
		dto.setBuyCount(existingUser.getBuyCount());
		
		// 비밀번호 변경 실행
		int result = service.modify(dto);
		
		// 결과 처리
		UserDTO updatedUser = service.getUserByEmail(userEmail);
		model.addAttribute("user", updatedUser);
		model.addAttribute("result", result);
		model.addAttribute("pwdChangeSuccess", result == 1);
		model.addAttribute("activeSection", "change-password"); // 활성 섹션 지정
		
		return "/user/myPage";
	}
		
	// 회원 탈퇴 처리
	@PostMapping("/user/deleteAccount")
	public String deleteAccount(HttpServletRequest request, HttpSession session, Model model) {
		// 세션에서 로그인한 사용자 이메일 가져오기
		String userEmail = (String) session.getAttribute("userEmail");
		
		// 로그인하지 않은 경우
		if(userEmail == null || userEmail.trim().isEmpty()) {
			return "redirect:/user/loginForm";
		}
		
		// 사용자 정보 조회
		UserDTO user = service.getUserByEmail(userEmail);
		if(user == null) {
			model.addAttribute("result", 0);
			model.addAttribute("message", "사용자 정보를 찾을 수 없습니다.");
			model.addAttribute("activeSection", "withdraw");
			return "/user/myPage";
		}
		
		// 입력한 비밀번호 가져오기
		String inputPassword = request.getParameter("password");
		
		// 비밀번호 입력 확인
		if(inputPassword == null || inputPassword.trim().isEmpty()) {
			model.addAttribute("result", 0);
			model.addAttribute("message", "비밀번호를 입력해주세요.");
			model.addAttribute("user", user);
			model.addAttribute("activeSection", "withdraw");
			return "/user/myPage";
		}
		
		// DB에서 가져온 사용자의 비밀번호와 입력한 비밀번호 비교
		if(!inputPassword.equals(user.getPassword())) {
			model.addAttribute("result", 0);
			model.addAttribute("message", "비밀번호가 일치하지 않습니다.");
			model.addAttribute("user", user);
			model.addAttribute("activeSection", "withdraw");
			return "/user/myPage";
		}
		
		// 회원 탈퇴 실행 (DB에서 데이터 삭제)
		int result = service.deleteUser(user.getUserId());
		
		if(result == 1) {
			// 세션 초기화
			session.invalidate();
			model.addAttribute("result", result);
			model.addAttribute("message", "회원탈퇴가 완료되었습니다.");
			return "/user/logout";
		} else {
			model.addAttribute("result", 0);
			model.addAttribute("message", "회원탈퇴에 실패했습니다. 다시 시도해주세요.");
			model.addAttribute("user", user);
			model.addAttribute("activeSection", "withdraw");
			return "/user/myPage";
		}
	}
	// 아이디 찾기 폼 페이지
	@GetMapping("/user/findId")
	public String findIdForm() {
		return "/user/findIdForm";
	}
	
	// 아이디 찾기 처리
	@PostMapping("/user/findIdResult")
	public String findIdResult(HttpServletRequest request, Model model) {
		String username = request.getParameter("username");
		String phone = request.getParameter("phone");
		
		// 전화번호 포맷팅 (숫자만 입력해도 하이픈 추가)
		String formattedPhone = formatPhoneNumber(phone);
		
		// 이름과 전화번호로 사용자 조회
		UserDTO user = service.getUserByNameAndPhone(username, formattedPhone);
		
		if(user != null) {
			// 찾기 성공
			model.addAttribute("result", 1);
			model.addAttribute("username", user.getUsername());
			model.addAttribute("email", user.getEmail());
		} else {
			// 찾기 실패
			model.addAttribute("result", 0);
		}
		
		return "/user/findId";
	}
		
	// 회원가입 이메일 인증번호 발송
	@PostMapping("/user/sendVerificationCode")
	@ResponseBody
	public String sendVerificationCode(HttpServletRequest request, HttpSession session) {
		String email = request.getParameter("email");
		
		if(email == null || email.trim().isEmpty()) {
			return "{\"result\": 0, \"message\": \"이메일을 입력해주세요.\"}";
		}
		
		// 이메일 중복 체크
		if(service.isExistEmail(email)) {
			return "{\"result\": -1, \"message\": \"이미 사용 중인 이메일입니다.\"}";
		}
		
		// 인증번호 생성
		String verificationCode = emailService.generateVerificationCode();
		
		// 세션에 저장 (5분 유효)
		session.setAttribute("signupVerificationCode", verificationCode);
		session.setAttribute("signupVerificationEmail", email);
		session.setMaxInactiveInterval(300); // 5분
		
		// 이메일 발송 (회원가입 타입)
		boolean emailSent = emailService.sendVerificationEmail(email, verificationCode, "signup");
		
		if(emailSent) {
			return "{\"result\": 1, \"message\": \"인증번호가 발송되었습니다.\"}";
		} else {
			return "{\"result\": 0, \"message\": \"이메일 발송에 실패했습니다.\"}";
		}
	}
	
	// 회원가입 이메일 인증번호 확인
	@PostMapping("/user/verifySignupCode")
	@ResponseBody
	public String verifySignupCode(HttpServletRequest request, HttpSession session) {
		String inputCode = request.getParameter("code");
		String email = request.getParameter("email");
		
		String sessionCode = (String) session.getAttribute("signupVerificationCode");
		String sessionEmail = (String) session.getAttribute("signupVerificationEmail");
		
		if(sessionCode == null || sessionEmail == null) {
			return "{\"result\": -1, \"message\": \"인증 시간이 만료되었습니다. 다시 시도해주세요.\"}";
		}
		
		if(!sessionEmail.equals(email)) {
			return "{\"result\": 0, \"message\": \"이메일이 일치하지 않습니다.\"}";
		}
		
		if(sessionCode.equals(inputCode)) {
			// 인증 성공 - 인증 완료 플래그 설정
			session.setAttribute("emailVerified", true);
			session.setAttribute("verifiedEmail", email);
			return "{\"result\": 1, \"message\": \"인증이 완료되었습니다.\"}";
		} else {
			return "{\"result\": 0, \"message\": \"인증번호가 일치하지 않습니다.\"}";
		}
	}
	
	// 비밀번호 찾기 폼 페이지
	@GetMapping("/user/findPwd")
	public String findPwdForm() {
		return "/user/findPwd";
	}
	
	// 비밀번호 찾기 처리 (인증번호 발송)
	@PostMapping("/user/findPwdResult")
	public String findPwdResult(HttpServletRequest request, HttpSession session, Model model) {
		String email = request.getParameter("email");
		String username = request.getParameter("username");
		String phone = request.getParameter("phone");
		
		// 전화번호 포맷팅 (숫자만 입력해도 하이픈 추가)
		String formattedPhone = formatPhoneNumber(phone);
		
		// 이메일, 이름, 전화번호로 사용자 조회
		UserDTO user = service.getUserByEmailAndNameAndPhone(email, username, formattedPhone);
		
		if(user != null) {
			// 사용자 정보 찾기 성공 - 인증번호 생성 및 발송
			String verificationCode = emailService.generateVerificationCode();
			
			// 인증번호를 세션에 저장 (5분 유효)
			session.setAttribute("verificationCode", verificationCode);
			session.setAttribute("verificationEmail", email);
			session.setAttribute("verificationUser", user);
			session.setMaxInactiveInterval(300); // 5분
			
			// 이메일 발송 (비밀번호 찾기 타입)
			boolean emailSent = false;
			emailSent = emailService.sendVerificationEmail(email, verificationCode, "findPassword");
			
			if(emailSent) {
				model.addAttribute("result", 1);
				model.addAttribute("email", email);
			} else {
				// 이메일 발송 실패
				model.addAttribute("result", -1);
			}
		} else {
			// 사용자 정보 없음
			model.addAttribute("result", 0);
		}
		
		return "/user/findPwdResult";
	}
	
	// 인증번호 확인 및 비밀번호 표시
	@PostMapping("/user/verifyCode")
	public String verifyCode(HttpServletRequest request, HttpSession session, Model model) {
		String inputCode = request.getParameter("verificationCode");
		String sessionCode = (String) session.getAttribute("verificationCode");
		UserDTO user = (UserDTO) session.getAttribute("verificationUser");
		
		if(sessionCode == null || user == null) {
			// 세션 만료
			model.addAttribute("result", -1);
			model.addAttribute("message", "세션이 만료되었습니다. 다시 시도해주세요.");
			return "/user/verifyCodeResult";
		}
		
		if(sessionCode.equals(inputCode)) {
			// 인증 성공
			model.addAttribute("result", 1);
			model.addAttribute("username", user.getUsername());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("password", user.getPassword());
			
			// 인증 완료 후 세션 정보 삭제
			session.removeAttribute("verificationCode");
			session.removeAttribute("verificationEmail");
			session.removeAttribute("verificationUser");
		} else {
			// 인증 실패
			model.addAttribute("result", 0);
			model.addAttribute("message", "인증번호가 일치하지 않습니다.");
		}
		
		return "/user/verifyCodeResult";
	}
		
		// ====== /user 경로 (userWriteForm.html용) ======
		
	// 회원가입 처리 - HTML 폼 필드명과 DTO 필드명 매핑
	@PostMapping("/user/write")
	public String userWrite(HttpServletRequest request, 
	                        @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
	                        HttpSession session,
	                        Model model) {
		// HTML 폼 필드 받기
		String email = request.getParameter("email"); // 이메일 (로그인 ID로 사용)
		String pwd = request.getParameter("pwd");
		String name = request.getParameter("name");
		String tel = request.getParameter("tel");
		String zipcode = request.getParameter("zipcode");
		String addrRoad = request.getParameter("addrRoad");
		String addrJibun = request.getParameter("addrJibun");
		String addrDetail = request.getParameter("addrDetail");
		String introduce = request.getParameter("introduce");
		
		// 필수 필드 검증
		if(email == null || email.trim().isEmpty() || 
		   pwd == null || pwd.trim().isEmpty() || 
		   name == null || name.trim().isEmpty()) {
			model.addAttribute("result", 0);
			model.addAttribute("message", "필수 항목을 모두 입력해주세요.");
			return "/user/write";
		}
		
		// 이메일 인증 확인
		Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
		String verifiedEmail = (String) session.getAttribute("verifiedEmail");
		
		if(emailVerified == null || !emailVerified || !email.equals(verifiedEmail)) {
			model.addAttribute("result", -2);
			model.addAttribute("message", "이메일 인증을 완료해주세요.");
			model.addAttribute("email", email);
			model.addAttribute("name", name);
			return "/user/write";
		}
		
		// 이메일 중복 체크 (이중 확인)
		if(service.isExistEmail(email)) {
			model.addAttribute("result", -1);
			model.addAttribute("message", "이미 사용 중인 이메일입니다. 다른 이메일을 사용해주세요.");
			model.addAttribute("email", email);
			model.addAttribute("name", name);
			return "/user/write";
		}
		
		// 전화번호 포맷팅 (숫자만 입력받아서 하이픈 추가)
		String formattedPhone = formatPhoneNumber(tel);
		
		// 프로필 이미지 처리
		String profileImagePath = null;
		System.out.println("🖼️ 회원가입 - 프로필 이미지 파일: " + (profileImage != null ? profileImage.getOriginalFilename() : "null"));
		System.out.println("🖼️ 파일 비어있음? " + (profileImage != null ? profileImage.isEmpty() : "null"));
		
		if(profileImage != null && !profileImage.isEmpty()) {
			System.out.println("✅ 프로필 이미지 저장 시작...");
			profileImagePath = saveProfileImage(profileImage);
			System.out.println("✅ 저장된 경로: " + profileImagePath);
		} else {
			System.out.println("⚠️ 프로필 이미지가 선택되지 않았거나 비어있음");
		}
		
		// DTO에 매핑
		UserDTO dto = new UserDTO();
		dto.setEmail(email); // 이메일을 로그인 ID로 사용
		dto.setPassword(pwd);
		dto.setUsername(name);
		dto.setPhone(formattedPhone); // 포맷팅된 전화번호
		dto.setProfileImage(profileImagePath); // 프로필 이미지 경로
		dto.setAddr1(zipcode); // 우편번호
		dto.setAddr2(addrRoad); // 도로명주소
		dto.setAddr3(addrJibun); // 지번주소
		dto.setAddr4(addrDetail); // 상세주소
		dto.setIntroduce(introduce); // 자기소개
		// createdAt, updatedAt은 JPA의 @CreationTimestamp, @UpdateTimestamp가 자동 처리
		
		int result = service.userWrite(dto);
		
		// 회원가입 성공 시 인증 세션 정보 삭제
		if(result == 1) {
			session.removeAttribute("emailVerified");
			session.removeAttribute("verifiedEmail");
			session.removeAttribute("signupVerificationCode");
			session.removeAttribute("signupVerificationEmail");
		}
		
		// result: 1=성공, 0=실패, -1=이메일 중복, -2=이메일 인증 필요
		model.addAttribute("result", result);
		model.addAttribute("email", email);
		model.addAttribute("name", name);
		model.addAttribute("phone", formattedPhone);
		if(result == 1) {
				model.addAttribute("message", "회원가입이 완료되었습니다.");
			} else if(result == -1) {
				model.addAttribute("message", "이미 사용 중인 이메일입니다.");
			} else {
				model.addAttribute("message", "회원가입에 실패했습니다. 다시 시도해주세요.");
			}
			return "/user/write";
		}
		
	// 이메일 중복 체크
	@GetMapping("/user/checkEmail")
	public String checkEmail(HttpServletRequest request, Model model) {
		String email = request.getParameter("email");
		
		boolean isExist = false;
		if(email != null && !email.trim().isEmpty()) {
			isExist = service.isExistEmail(email);
		}
		
		model.addAttribute("isExist", isExist);
		model.addAttribute("email", email);
		return "/user/checkEmail";
	}
	
	// 전화번호 중복 체크 (Ajax 요청용)
	@GetMapping("/user/checkPhone")
	@ResponseBody
	public Map<String, Object> checkPhone(@RequestParam("phone") String phone) {
		Map<String, Object> result = new HashMap<>();
		
		boolean isExist = false;
		if(phone != null && !phone.trim().isEmpty()) {
			isExist = service.isExistPhone(phone);
		}
		
		result.put("isExist", isExist);
		result.put("phone", phone);
		
		return result;
	}
	
	// 전화번호 포맷팅 헬퍼 메서드 (숫자만 입력받아서 하이픈 추가)
	private String formatPhoneNumber(String phone) {
		if(phone == null || phone.trim().isEmpty()) {
			return phone;
		}
		
		// 숫자만 추출
		String numbersOnly = phone.replaceAll("[^0-9]", "");
		
		// 전화번호 길이에 따라 포맷팅
		if(numbersOnly.length() == 11) {
			// 11자리: 010-1234-5678
			return numbersOnly.substring(0, 3) + "-" + 
			       numbersOnly.substring(3, 7) + "-" + 
			       numbersOnly.substring(7, 11);
		} else if(numbersOnly.length() == 10) {
			// 10자리: 010-123-4567
			return numbersOnly.substring(0, 3) + "-" + 
			       numbersOnly.substring(3, 6) + "-" + 
			       numbersOnly.substring(6, 10);
		}
		
		// 형식이 맞지 않으면 원본 반환
		return phone;
	}
	
	// 프로필 이미지 파일 저장
	private String saveProfileImage(MultipartFile file) {
		if(file == null || file.isEmpty()) {
			System.out.println("⚠️ 프로필 이미지가 비어있음");
			return null;
		}
		
		try {
			// 업로드 디렉토리 생성
			Path uploadPath = Paths.get(uploadDir);
			System.out.println("📁 프로필 업로드 디렉토리: " + uploadPath.toAbsolutePath());
			
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
				System.out.println("✅ 디렉토리 생성 완료: " + uploadPath.toAbsolutePath());
			}
			
			// 파일 확장자 추출
			String originalFilename = file.getOriginalFilename();
			String extension = "";
			if(originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}
			
			// UUID로 고유 파일명 생성
			String filename = UUID.randomUUID().toString() + extension;
			
			// 파일 저장
			Path filePath = uploadPath.resolve(filename);
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("💾 프로필 이미지 저장 완료: " + filePath.toAbsolutePath());
			System.out.println("🌐 웹 경로: /uploads/profiles/" + filename);
			
			// DB에 저장할 상대 경로 반환 (웹에서 접근 가능한 경로)
			return "/uploads/profiles/" + filename;
		} catch (Exception e) {
			// 파일 저장 실패 시 null 반환 (프로필 사진은 선택사항)
			System.out.println("❌ 프로필 이미지 저장 실패: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * ✅ 사용자 정보 페이지 (평점, 판매중인 물품 목록)
	 */
	@GetMapping("/user/userInfo/{userId}")
	public String userInfo(
			@PathVariable("userId") Integer userId,
			@RequestParam(name = "pg", defaultValue = "1") int pg,
			Model model) {
		
		// 사용자 정보 조회
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
		
		// 해당 사용자가 판매중인 상품 조회 (페이징)
		Pageable pageable = PageRequest.of(pg - 1, 8, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Posts> userPosts = postRepository.findByUserIdAndStatus(userId, "SELLING", pageable);
		
		int totalPages = userPosts.getTotalPages();
		if (totalPages == 0) totalPages = 1;
		int startPage = (pg - 1) / 10 * 10 + 1;
		int endPage = Math.min(startPage + 9, totalPages);
		
		String userIdStr = String.valueOf(userId);
		
		// 리뷰 정보 가져오기
		List<com.example.dtem.dto.ReviewWithUserDTO> receivedReviews = reviewService.getReceivedReviewsWithUser(userIdStr);
		Double averageRating = reviewService.getAverageRating(userIdStr);
		Long reviewCount = reviewService.getReviewCount(userIdStr);
		
		// 거래 횟수 가져오기
		Long sellCount = tradeService.countCompletedSales(userIdStr);
		Long buyCount = tradeService.countCompletedPurchases(userIdStr);
		
		model.addAttribute("user", user);
		model.addAttribute("posts", userPosts.getContent());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("currentPage", pg);
		
		// 리뷰 데이터 추가
		model.addAttribute("receivedReviews", receivedReviews);
		model.addAttribute("averageRating", averageRating);
		model.addAttribute("reviewCount", reviewCount);
		model.addAttribute("sellCount", sellCount);
		model.addAttribute("buyCount", buyCount);
		
		return "user/userInfo";
	}
	
	// 리뷰 작성 페이지
	@GetMapping("/user/userReview")
	public String userReviewForm(@RequestParam("postId") Long postId,
	                              @RequestParam("revieweeId") String revieweeId,
	                              @RequestParam("tradeId") Long tradeId,
	                              HttpSession session,
	                              Model model) {
		Integer currentUserId = (Integer) session.getAttribute("userId");
		
		if (currentUserId == null) {
			return "redirect:/user/loginForm";
		}
		
		// 상품 정보 조회
		Posts post = postRepository.findById(postId.intValue()).orElse(null);
		
		// 리뷰 받을 사람 정보 조회
		Users reviewee = null;
		try {
			Integer revieweeIdInt = Integer.parseInt(revieweeId);
			reviewee = userRepository.findById(revieweeIdInt).orElse(null);
		} catch (NumberFormatException e) {
			// 무시
		}
		
		model.addAttribute("post", post);
		model.addAttribute("reviewee", reviewee);
		model.addAttribute("revieweeId", revieweeId);
		model.addAttribute("tradeId", tradeId);
		model.addAttribute("postId", postId);
		
		return "user/userReview";
	}
	
	// 리뷰 제출 처리
	@PostMapping("/user/submitReview")
	public String submitReview(@RequestParam("tradeId") Long tradeId,
	                           @RequestParam("revieweeId") String revieweeId,
	                           @RequestParam("postId") Long postId,
	                           @RequestParam("rating") Integer rating,
	                           @RequestParam(value = "responseSpeed", required = false) Integer responseSpeed,
	                           @RequestParam(value = "mannerScore", required = false) Integer mannerScore,
	                           @RequestParam(value = "willTradeAgain", required = false) String willTradeAgain,
	                           @RequestParam(value = "reviewContent", required = false) String reviewContent,
	                           HttpSession session,
	                           Model model) {
		Integer currentUserId = (Integer) session.getAttribute("userId");
		
		if (currentUserId == null) {
			return "redirect:/user/loginForm";
		}
		
		String reviewerId = String.valueOf(currentUserId);
		
		// 리뷰 엔티티 생성
		com.example.dtem.entity.Review review = com.example.dtem.entity.Review.builder()
				.tradeId(tradeId)
				.reviewerId(reviewerId)
				.revieweeId(revieweeId)
				.rating(rating)
				.responseSpeed(responseSpeed)
				.mannerScore(mannerScore)
				.willTradeAgain(willTradeAgain)
				.reviewContent(reviewContent)
				.build();
		
		try {
			reviewService.saveReview(review);
			
			// 리뷰 작성 후 성공 페이지로 이동
			session.setAttribute("reviewSuccess", true);
			session.setAttribute("activeSection", "buy-history");
			return "user/reviewSuccess";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "redirect:/user/userReview?postId=" + postId + 
			       "&revieweeId=" + revieweeId + "&tradeId=" + tradeId;
		}
	}
	
	// 찜 삭제 (myPage용)
	@PostMapping("/user/removeWishlist")
	@ResponseBody
	public Map<String, Object> removeWishlist(@RequestParam("postId") Integer postId,
	                                          HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		Integer userId = (Integer) session.getAttribute("userId");
		if (userId == null) {
			response.put("success", false);
			response.put("message", "로그인이 필요합니다.");
			return response;
		}
		
		try {
			// PostLike 삭제
			Optional<com.example.dtem.entity.PostLike> postLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
			if (postLike.isPresent()) {
				postLikeRepository.delete(postLike.get());
				
				// Posts의 wishlistCount 감소
				Posts post = postRepository.findById(postId).orElse(null);
				if (post != null && post.getWishlistCount() > 0) {
					post.setWishlistCount(post.getWishlistCount() - 1);
					postRepository.save(post);
				}
				
				response.put("success", true);
			} else {
				response.put("success", false);
				response.put("message", "찜 목록에 없는 상품입니다.");
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "찜 삭제 중 오류가 발생했습니다.");
		}
		
		return response;
	}
	
	// 판매 내역 숨기기
	@PostMapping("/user/hideSellHistory")
	@ResponseBody
	public Map<String, Object> hideSellHistory(@RequestParam("postId") Integer postId,
	                                           HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			@SuppressWarnings("unchecked")
			List<Integer> hiddenSellPosts = (List<Integer>) session.getAttribute("hiddenSellPosts");
			if (hiddenSellPosts == null) {
				hiddenSellPosts = new java.util.ArrayList<>();
			}
			
			if (!hiddenSellPosts.contains(postId)) {
				hiddenSellPosts.add(postId);
				session.setAttribute("hiddenSellPosts", hiddenSellPosts);
			}
			
			response.put("success", true);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", e.getMessage());
		}
		
		return response;
	}
	
	// 구매 내역 숨기기
	@PostMapping("/user/hideBuyHistory")
	@ResponseBody
	public Map<String, Object> hideBuyHistory(@RequestParam("tradeId") Long tradeId,
	                                          HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			@SuppressWarnings("unchecked")
			List<Long> hiddenBuyTrades = (List<Long>) session.getAttribute("hiddenBuyTrades");
			if (hiddenBuyTrades == null) {
				hiddenBuyTrades = new java.util.ArrayList<>();
			}
			
			if (!hiddenBuyTrades.contains(tradeId)) {
				hiddenBuyTrades.add(tradeId);
				session.setAttribute("hiddenBuyTrades", hiddenBuyTrades);
			}
			
			response.put("success", true);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", e.getMessage());
		}
		
		return response;
	}

}

