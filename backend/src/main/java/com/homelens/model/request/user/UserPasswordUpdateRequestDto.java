package com.homelens.model.request.user;

import lombok.Data;
import lombok.ToString;

@Data
public class UserPasswordUpdateRequestDto {
	private int userId;
	@ToString.Exclude
	private String userPw;
}
