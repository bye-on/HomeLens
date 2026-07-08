package com.homelens.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.homelens.common.SuccessResponse;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.response.user.UserDetailResponseDto;
import com.homelens.model.response.user.UserListResponseDto;
import com.homelens.model.service.user.UserService;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
	
	private final UserService userService;
	
	@GetMapping("user")
	ResponseEntity<?> getUserList(@ModelAttribute PageRequestDto pageDto){
		
		UserListResponseDto userList = userService.getAllUsers(pageDto);
		
		return ResponseEntity.status(200).body(new SuccessResponse<>("유저 조회 성공", userList));
	}
	
	@PostMapping("user/{userId}")
	ResponseEntity<?> softDeleteUser(@PathVariable int userId ){
		userService.softDeleteUsers(userId);
		
		return ResponseEntity.status(200).body(new SuccessResponse<>("유저 삭제 성공"));
	}
 
}
