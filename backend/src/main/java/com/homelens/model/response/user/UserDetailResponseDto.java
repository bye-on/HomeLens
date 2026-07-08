package com.homelens.model.response.user;

import lombok.Data;

@Data
public class UserDetailResponseDto {

	private int userId;
	private String userEmail;
	private String userName;
	private String userTel;
	private String userRole;
	private String userEmailVerified;
	private String userStatus;
	private String userCreatedAt;
}
