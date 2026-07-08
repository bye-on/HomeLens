package com.homelens.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.user.JoinRequestDto;
import com.homelens.model.request.user.UserDetailUpdateRequestDto;
import com.homelens.model.request.user.UserPasswordResetRequestDto;
import com.homelens.model.request.user.UserPasswordUpdateRequestDto;
import com.homelens.model.response.user.UserDetailResponseDto;
import com.homelens.model.response.user.UserResponseDto;

@Mapper
public interface UserMapper {

	int existsByUsername(String username);
	
	int existsByEmail(String username);
	
	int existsByUserId(int userId);

	void signUp(JoinRequestDto joinDto);
	
	UserResponseDto findByUsername(String username);
	
	UserResponseDto findByEmail(String email);
	
	int updateUserRefreshToken(UserResponseDto userDto);
	
	int setEmailVerified(int userId);
	
	UserDetailResponseDto findByUserId(int userId);
	
	List<UserDetailResponseDto> getAllUsers(PageRequestDto pageDto);
	
	void softDeleteUsers(int userId);
	
	void logout(int userId);
	
	int updateUserInfo(UserDetailUpdateRequestDto userDetailUpdateRequestDto);
	
	int updateUserPassword(UserPasswordUpdateRequestDto userPasswordUpdateRequestDto);
	
	int countUser();
}
