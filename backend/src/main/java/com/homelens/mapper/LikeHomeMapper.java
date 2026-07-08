package com.homelens.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.homelens.model.request.likeHome.LikeHomeRequestDto;
import com.homelens.model.request.likeHome.LikeHomeRequestWithKeyDto;
import com.homelens.model.response.likeHome.LikeHomeResponseDto;
import com.homelens.model.response.property.PopularPropertyResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;

@Mapper
public interface LikeHomeMapper {

	int exists(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto);
	int insertByKey(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto);
	int deleteByKey(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto);
	int countByItemId(@Param("itemId") int itemId);
	List<LikeHomeResponseDto> findByUserId(int userId);
	List<PopularPropertyResponseDto> findTop5LikedPropertiesLast7Days(int userId);
}
