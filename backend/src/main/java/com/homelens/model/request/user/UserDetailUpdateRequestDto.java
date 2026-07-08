package com.homelens.model.request.user;

import lombok.Data;

@Data
public class UserDetailUpdateRequestDto {
	private int userId;
	private String userName;
	private String userTel;
	
}
