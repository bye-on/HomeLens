package com.homelens.model.request.user;

import lombok.Data;
import lombok.ToString;

@Data
public class UserPasswordResetRequestDto {
	private String userEmail;
	@ToString.Exclude
	private String userPw;
}
