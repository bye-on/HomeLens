package com.homelens.model.service.likeHome;

import java.util.List;

import com.homelens.model.request.likeHome.LikeHomeRequestDto;
import com.homelens.model.request.likeHome.LikeHomeRequestWithKeyDto;
import com.homelens.model.response.likeHome.LikeHomeResponseDto;
import com.homelens.model.response.property.PopularPropertyResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;

public interface LikeHomeService {
	boolean toggleLike(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto);
    int countByItemId(int itemId);
    boolean isLiked(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto);
    List<LikeHomeResponseDto> getMyLikes(int userId);
    public List<PopularPropertyResponseDto> getTop5LikedPropertiesLast7Days(int userId);
}
