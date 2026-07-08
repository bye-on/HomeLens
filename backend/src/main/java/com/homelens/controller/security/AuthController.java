package com.homelens.controller.security;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homelens.common.ErrorResponse;
import com.homelens.common.SuccessResponse;
import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.model.CustomUserDetails;
import com.homelens.model.request.user.LoginRequestDto;
import com.homelens.model.response.user.UserResponseDto;
import com.homelens.model.service.user.EmailVerifyService;
import com.homelens.model.service.user.UserService;
import com.homelens.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final JwtUtil jwtUtil;
	private final UserService userService;
	
	private final EmailVerifyService emailVerifyService;

	public record VerifyEmailRequest(String token) {
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshAccessTokne(@RequestHeader("Refresh-Token") String refreshToken) {
		if (refreshToken == null || refreshToken.isEmpty()) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Refresh token is required"));
			throw new CustomException(ErrorCode.BAD_REQUEST, "갱신 토큰이 필요합니다.");
		}
		try {
			// 1. Refresh Token 유효성 검증 (JWT 자체) 및 이메일 추출
			Map<String, Object> claims = jwtUtil.getClaims(refreshToken);

			String email = (String) claims.get("userEmail");

			if (email == null) {
				throw new JwtException("Invalid refresh token");
			}

			// 2. DB에서 사용자 조회 및 Refresh Token 일치 여부 확인
			UserResponseDto userDto = userService.findByEmail(email);
			System.out.println("userDto: " + userDto);
			
			//사용자가 이메일 인증이 되었는지 다시 확인
			if (!Boolean.TRUE.equals(userDto.getUserEmailVerified())) {
				throw new CustomException(ErrorCode.USER_FORBIDDEN, "이메일 인증 이전입니다.");
			}

			if (userDto == null || userDto.getUserRefreshToken() == null
					|| !userDto.getUserRefreshToken().equals(refreshToken)) {
				log.warn("Invalid or mismatched refresh token for user: {}", email);
				// 보안: DB의 토큰과 불일치 시, 해당 사용자의 DB 토큰을 무효화(null 처리)하는 것도 고려
				// memberService.invalidateRefreshToken(email);
				throw new CustomException(ErrorCode.USER_UNAUTHORIZED, "갱신 토큰이 유효하지 않습니다.");
			}

			// 3. 새 Access Token 생성
			String newAccessToken = jwtUtil.createAccessToken(userDto);

			// 4. Refresh Token Rotation: 새 Refresh Token 생성 및 DB 업데이트 - 보안 상 권장
			String newRefreshToken = jwtUtil.createRefreshToken(userDto);
			userDto.setUserRefreshToken(newRefreshToken); // Member 객체에 새 Refresh Token 설정
			userService.updateUserRefreshToken(userDto); // DB에 새 Refresh Token 저장
//			System.out.println("새로운 리프레시 토큰: " + newRefreshToken);
			
			Map<String, Object> tokenMap = new HashMap<>();
			tokenMap.put("newAccessToken", newAccessToken);
			tokenMap.put("newRefreshToken", newRefreshToken);
			
			// 5. 새 토큰들을 응답으로 반환
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(new SuccessResponse<>("갱신 토큰 인증 성공", tokenMap));
		} catch (JwtException e) {

			log.warn("Refresh token validation failed: {}", e.getMessage());
			Map<String, Object> errorDetails = new HashMap<>();
			errorDetails.put("status", HttpStatus.UNAUTHORIZED);
			errorDetails.put("message", "INVALID_REFRESH_TOKEN");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
		}
	}

	@GetMapping("/email/verify")
	public ResponseEntity<?> verify(@RequestParam("token") String token) {
		emailVerifyService.verify(token);
		URI target = URI.create("http://localhost:5173/");
		return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequestDto loginDto){
		return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("로그인 성공"));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<?> logout(Authentication authentication){
		int userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
		userService.logout(userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new SuccessResponse<>("로그아웃"));
				
	}
}
