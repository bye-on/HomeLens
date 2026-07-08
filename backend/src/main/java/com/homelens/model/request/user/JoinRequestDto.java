package com.homelens.model.request.user;

import lombok.Data;
import lombok.ToString;

@Data
public class JoinRequestDto {

	private String userEmail;
	@ToString.Exclude
	private String userPw;
	private String userName;
	private String userTel;
}
