package com.homelens.model.request.user;

import lombok.Data;
import lombok.ToString;

@Data
public class LoginRequestDto {

	private String email;
	@ToString.Exclude
	private String password;
}
