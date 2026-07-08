package com.homelens.model.response.user;

import lombok.Data;

@Data
public class UserResponseDto {
	private int userId;
	private String userEmail;
	private String userPw;
	private String userName;
	private String userTel;
	private String userRole;
	private String userCreatedAt;
	private String userRefreshToken;
	private Boolean userEmailVerified;
	private String userStatus;
}
