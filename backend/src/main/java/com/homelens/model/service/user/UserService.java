package com.homelens.model.service.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.user.JoinRequestDto;
import com.homelens.model.request.user.UserDetailUpdateRequestDto;
import com.homelens.model.request.user.UserPasswordResetRequestDto;
import com.homelens.model.request.user.UserPasswordUpdateRequestDto;
import com.homelens.model.response.user.UserDetailResponseDto;
import com.homelens.model.response.user.UserListResponseDto;
import com.homelens.model.response.user.UserResponseDto;

public interface UserService {

	void joinProcess(JoinRequestDto joinDto);
	
	void updateUserRefreshToken(UserResponseDto userDto);
	
	int existsByUsername(String username);
	
	int existsByEmail(String username);
	
	int existsByUserId(int userId);

	void signUp(JoinRequestDto joinDto);
	
	UserResponseDto findByUsername(String username);
	
	UserResponseDto findByEmail(String email);
	
	UserDetailResponseDto findByUserId(Integer userId);
	
	UserListResponseDto getAllUsers(PageRequestDto pageRequestDto);
	
	void softDeleteUsers(int userId);
	
	void logout(int userId);
	
	void updateUserInfo(UserDetailUpdateRequestDto userDeatilDetailUpdateRequestDto);
	
	void updateUserPassword(UserPasswordUpdateRequestDto userPasswordUpdateRequestDto);

	void resetUserPassword(UserPasswordResetRequestDto userPasswordResetRequestDto);
	
	int countAll();
}
