package com.homelens.model.service.user;

public interface EmailVerifyService {

	public void verify(String rawToken);
}
