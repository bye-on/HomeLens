package com.homelens.mapper;

import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.property.MapSearchRequestDto;
import com.homelens.model.request.property.PropertySearchByIdRequestDto;
import com.homelens.model.request.property.PropertySearchRequestDto;
import com.homelens.model.request.property.RegionSearchRequestDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PropertyMapper {

    // 1) ?꾨쿋??+ ?섎뱶?꾪꽣 TopK
    List<PropertyDetailResponseDto> selectBySemanticFilters(PropertySearchRequestDto query);

    // 2) 吏???뺣낫留뚯쑝濡??꾪꽣留곷맂 由ъ뒪??
    List<PropertyDetailResponseDto> selectByRegion(PropertySearchRequestDto query);

    List<PropertyDetailResponseDto> selectByBasicSearch(PropertySearchRequestDto query);

    List<PropertyDetailResponseDto> selectMapProperties(MapSearchRequestDto query);
    
    PropertyDetailResponseDto selectPropertyByItemId(PropertySearchByIdRequestDto propertySearchByIdRequestDto);
    
    List<PropertyDetailResponseDto> selectRecentProperty(PageRequestDto pageDto);
    
    List<String> selectLocal1(RegionSearchRequestDto regionSearchDto);
    List<String> selectLocal2(RegionSearchRequestDto regionSearchDto);
    List<String> selectLocal3(RegionSearchRequestDto regionSearchDto);
    
    int countProperty();
}
