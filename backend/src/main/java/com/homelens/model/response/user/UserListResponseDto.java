package com.homelens.model.response.user;

import java.util.List;

import lombok.Data;

@Data
public class UserListResponseDto {
	private List<UserDetailResponseDto> userList;
	private int totalPage;
	private int currentPage;
}
