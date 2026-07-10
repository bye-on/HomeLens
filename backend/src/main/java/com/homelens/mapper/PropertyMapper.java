package com.homelens.mapper;

import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.property.MapSearchRequestDto;
import com.homelens.model.request.property.PropertySearchByIdRequestDto;
import com.homelens.model.request.property.PropertySearchRequestDto;
import com.homelens.model.request.property.RegionSearchRequestDto;
import com.homelens.model.response.property.MapCoordinateResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PropertyMapper {

    // 1) 의미 기반 필터와 벡터 유사도를 적용한 상위 매물 조회
    List<PropertyDetailResponseDto> selectBySemanticFilters(PropertySearchRequestDto query);

    // 2) 동일 지역의 대체 매물 조회
    List<PropertyDetailResponseDto> selectByRegion(PropertySearchRequestDto query);

    List<PropertyDetailResponseDto> selectByBasicSearch(PropertySearchRequestDto query);

    List<MapCoordinateResponseDto> selectMapProperties(MapSearchRequestDto query);

    List<PropertyDetailResponseDto> selectMapPropertyItems(MapSearchRequestDto query);

    PropertyDetailResponseDto selectPropertyByItemId(PropertySearchByIdRequestDto propertySearchByIdRequestDto);

    List<PropertyDetailResponseDto> selectRecentProperty(PageRequestDto pageDto);

    List<String> selectLocal1(RegionSearchRequestDto regionSearchDto);
    List<String> selectLocal2(RegionSearchRequestDto regionSearchDto);
    List<String> selectLocal3(RegionSearchRequestDto regionSearchDto);

    int countProperty();
}
