package com.homelens.model.service.property;

import java.util.List;

import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.property.MapSearchRequestDto;
import com.homelens.model.request.property.PropertySearchByIdRequestDto;
import com.homelens.model.request.property.RegionSearchRequestDto;
import com.homelens.model.request.property.SearchRequestDto;
import com.homelens.model.response.property.PropertyDetailListResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;
import com.homelens.model.response.property.RegionSearchResponseDto;
import com.homelens.model.response.property.SearchResultResponseDto;

public interface PropertySearchService {
	public SearchResultResponseDto propertySearch(SearchRequestDto req);
	public SearchResultResponseDto propertyBasicSearch(SearchRequestDto req);
	public List<PropertyDetailResponseDto> selectMapProperties(MapSearchRequestDto req);
	public PropertyDetailResponseDto selectPropertyByItemId(PropertySearchByIdRequestDto propertySearchByIdRequestDto);
	public PropertyDetailListResponseDto selectRecentProperty(PageRequestDto pageDto);
	public RegionSearchResponseDto selectLocal1(RegionSearchRequestDto regionSearchRequestDto);
	public RegionSearchResponseDto selectLocal2(RegionSearchRequestDto regionSearchRequestDto);
	public RegionSearchResponseDto selectLocal3(RegionSearchRequestDto regionSearchRequestDto);
	public Integer countAll();
}
