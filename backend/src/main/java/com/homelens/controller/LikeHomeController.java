package com.homelens.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homelens.common.SuccessResponse;
import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.model.CustomUserDetails;
import com.homelens.model.request.likeHome.LikeHomeRequestDto;
import com.homelens.model.request.likeHome.LikeHomeRequestWithKeyDto;
import com.homelens.model.response.likeHome.LikeHomeResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;
import com.homelens.model.service.likeHome.LikeHomeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Tag(name = "Like Home Controller", description = "관심 매물 처리 컨트롤러")
public class LikeHomeController {

	private final LikeHomeService likeHomeService;

	// 관심매물 토글
	@Operation(summary = "관심 매물 토글", description = "관심 매물을 추가하거나 삭제한다.")
	@PostMapping("/toggle")
	public ResponseEntity<SuccessResponse<Boolean>> toggleLike(@RequestBody LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto, Authentication authentication) {
		if(authentication == null) {
    		throw new CustomException(ErrorCode.USER_UNAUTHORIZED, "로그인이 필요합니다.");
    	}
		
		likeHomeRequestWithKeyDto.setUserId(((CustomUserDetails) authentication.getPrincipal()).getId());
		
		boolean liked = likeHomeService.toggleLike(likeHomeRequestWithKeyDto);
		return ResponseEntity.ok(
				new SuccessResponse<>(liked ? "관심 매물 추가 성공" : "관심 매물 삭제 성공", liked)
		);
	}

	// 이미 찜했는지 확인
	@Operation(summary = "찜 확인", description = "이미 찜을 했는지 확인한다.")
	@GetMapping("/check")
	public ResponseEntity<SuccessResponse<Boolean>> isLiked(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto) {
		boolean liked = likeHomeService.isLiked(likeHomeRequestWithKeyDto);
		return ResponseEntity.ok(
				new SuccessResponse<>(liked ? "이미 찜을 했습니다." : "아직 찜을 하지 않았습니다.", liked)
		);
	}

	// 내 관심매물 목록
	@Operation(summary = "관심 매물 목록 보기", description = "내 관심 매물 목록을 조회한다.")
	@GetMapping("/my")
	public ResponseEntity<SuccessResponse<List<LikeHomeResponseDto>>> getMyLikes(Authentication authentication) {
		if(authentication == null) {
    		throw new CustomException(ErrorCode.USER_UNAUTHORIZED, "로그인이 필요합니다.");
    	}
		
		int userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
		
		return ResponseEntity.ok(
				new SuccessResponse<>("관심 매물 조회 성공", likeHomeService.getMyLikes(userId)));
	}
	
	@GetMapping("/popular/top5")
	public ResponseEntity<?> top5Liked7d(Authentication authentication) {
		int userId = 0;
		
		if(authentication != null) {
			userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
		}
		
	    return ResponseEntity.ok(new SuccessResponse<>("최근 7일 인기 매물 조회 성공",
	            likeHomeService.getTop5LikedPropertiesLast7Days(userId)));
	}

}
