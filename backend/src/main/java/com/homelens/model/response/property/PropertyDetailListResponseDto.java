package com.homelens.model.response.property;

import java.util.List;

import lombok.Data;

@Data
public class PropertyDetailListResponseDto {
	private List<PropertyDetailResponseDto> recentProperty;
	private int totalPage;
	private int currentPage;
}
