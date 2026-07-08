package com.homelens.model.service.likeHome;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.mapper.LikeHomeMapper;
import com.homelens.model.request.likeHome.LikeHomeRequestDto;
import com.homelens.model.request.likeHome.LikeHomeRequestWithKeyDto;
import com.homelens.model.response.likeHome.LikeHomeResponseDto;
import com.homelens.model.response.property.PopularPropertyResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeHomeServiceImpl implements LikeHomeService {

	private final LikeHomeMapper likeHomeMapper;

	@Override
	@Transactional
	public boolean toggleLike(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto) {

		if (likeHomeMapper.exists(likeHomeRequestWithKeyDto) > 0) {
			likeHomeMapper.deleteByKey(likeHomeRequestWithKeyDto);
			return false;
		} else {
			likeHomeMapper.insertByKey(likeHomeRequestWithKeyDto);
			return true;
		}
	}

	@Override
	public int countByItemId(int itemId) {
		return likeHomeMapper.countByItemId(itemId);
	}

	@Override
	public boolean isLiked(LikeHomeRequestWithKeyDto likeHomeRequestWithKeyDto) {
		return likeHomeMapper.exists(likeHomeRequestWithKeyDto) > 0;
	}

	@Override
	public List<LikeHomeResponseDto> getMyLikes(int userId) {
		List<LikeHomeResponseDto> list = likeHomeMapper.findByUserId(userId);
		if(list == null) {
			throw new CustomException(ErrorCode.NOT_FOUND, "관심 매물 목록을 찾을 수 없습니다.");
		}
		return list; 
	}
	
	@Override
	public List<PopularPropertyResponseDto> getTop5LikedPropertiesLast7Days(int userId) {
	    List<PopularPropertyResponseDto> list = likeHomeMapper.findTop5LikedPropertiesLast7Days(userId);
	    if (list == null || list.isEmpty()) {
	        throw new CustomException(ErrorCode.NOT_FOUND, "최근 7일 인기 매물이 없습니다.");
	    }
	    return list;
	}

}
