package com.homelens.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.homelens.common.SuccessResponse;
import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.model.CustomUserDetails;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.user.JoinRequestDto;
import com.homelens.model.request.user.UserDetailUpdateRequestDto;
import com.homelens.model.request.user.UserPasswordResetRequestDto;
import com.homelens.model.request.user.UserPasswordUpdateRequestDto;
import com.homelens.model.response.user.UserDetailResponseDto;
import com.homelens.model.service.user.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {


    private final UserService userService;


    @PostMapping
    public ResponseEntity<?> joinProcess(@RequestBody JoinRequestDto joinDto) {

        userService.joinProcess(joinDto);

        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("이메일 인증 성공 후 가입 완료됩니다."));
    }
    
    //로그인한 회원 정보
    @GetMapping("/me")
    public ResponseEntity<?> selectOneUserInfo(Authentication authentication){
    	if(authentication == null) {
    		throw new CustomException(ErrorCode.USER_UNAUTHORIZED, "로그인이 필요합니다.");
    	}

        int userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
    	
    	UserDetailResponseDto userDetailResponseDto = userService.findByUserId(userId);
    	
    	return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("유저 정보 반환 성공", userDetailResponseDto));
    }
    
    @PostMapping("/me")
    public ResponseEntity<?> softDeleteUsers(Authentication authentication){
    	
    	if(authentication == null) {
    		throw new CustomException(ErrorCode.USER_UNAUTHORIZED, "로그인이 필요합니다.");
    	}

        int userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
    	
    	userService.softDeleteUsers(userId);
    	return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("유저 삭제 성공"));
    }
    
    @PostMapping("/me/detail")
    public ResponseEntity<?> updateUserDetail(@RequestBody UserDetailUpdateRequestDto userDetailUpdateRequestDto, Authentication authentication){
    	
    	userDetailUpdateRequestDto.setUserId(((CustomUserDetails) authentication.getPrincipal()).getId());
    	
    	userService.updateUserInfo(userDetailUpdateRequestDto);
    	
    	System.out.println(userDetailUpdateRequestDto);
    	
    	return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("유저 수정 성공"));
    }
    
    @PostMapping("/me/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto, Authentication authentication){
    	
    	userPasswordUpdateRequestDto.setUserId(((CustomUserDetails) authentication.getPrincipal()).getId());
    	
    	userService.updateUserPassword(userPasswordUpdateRequestDto);
    	
    	return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("유저 비밀번호 변경 성공"));
    }

    @PostMapping("/me/password/reset")
    public ResponseEntity<?> resetUserPassword(@RequestBody UserPasswordResetRequestDto userPasswordResetRequestDto){
    	userService.resetUserPassword(userPasswordResetRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("유저 비밀번호 초기화 성공"));
    }
    
    @GetMapping("/count/all")
    public ResponseEntity<?> countAll(){
    	return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse<>("유저 조회 성공", userService.countAll()));
    }
}
