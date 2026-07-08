package com.homelens.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homelens.common.SuccessResponse;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.property.PropertySearchByIdRequestDto;
import com.homelens.model.request.property.RegionSearchRequestDto;
import com.homelens.model.request.property.SearchRequestDto;
import com.homelens.model.response.property.PropertyDetailListResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;
import com.homelens.model.response.property.RegionSearchResponseDto;
import com.homelens.model.response.property.SearchResultResponseDto;
import com.homelens.model.service.property.PropertySearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertySearchController {

	private final PropertySearchService propertySearchService;
	
	@PostMapping("/property-search")
	public ResponseEntity<SuccessResponse<SearchResultResponseDto>> search(@RequestBody SearchRequestDto req){
		SearchResultResponseDto result = "basic".equalsIgnoreCase(req.getMode())
				? propertySearchService.propertyBasicSearch(req)
				: propertySearchService.propertySearch(req);
		return ResponseEntity.ok(new SuccessResponse<>("부동산 조회 성공", result));
	}
		
	@PostMapping("/property-search/basic")
	public ResponseEntity<SuccessResponse<SearchResultResponseDto>> basicSearch(@RequestBody SearchRequestDto req){
		SearchResultResponseDto result = propertySearchService.propertyBasicSearch(req);
		return ResponseEntity.ok(new SuccessResponse<>("매물 일반 검색 성공", result));
	}
	@GetMapping("/{itemId}")
	public ResponseEntity<?> selectByItemId(@PathVariable int itemId){
		PropertySearchByIdRequestDto propertySearchByIdRequestDto = new PropertySearchByIdRequestDto();
		propertySearchByIdRequestDto.setItemId(itemId);
		
		PropertyDetailResponseDto result = propertySearchService.selectPropertyByItemId(propertySearchByIdRequestDto);
		
		return ResponseEntity.ok(new SuccessResponse<>("留ㅻЪ 議고쉶 ?깃났", result));
	}
	
	@GetMapping("/local")
	public ResponseEntity<?> selectLocal1(){
		RegionSearchRequestDto regionSearchRequestDto = new RegionSearchRequestDto();
		
		RegionSearchResponseDto result = propertySearchService.selectLocal1(regionSearchRequestDto);
		
		return ResponseEntity.ok(new SuccessResponse<>("local1 議고쉶 ?깃났", result));
	}
	
	@GetMapping("/local/{local1}")
	public ResponseEntity<?> selectLocal2(@PathVariable String local1){
		RegionSearchRequestDto regionSearchRequestDto = new RegionSearchRequestDto();
		regionSearchRequestDto.setLocal1(local1);
		
		RegionSearchResponseDto result = propertySearchService.selectLocal2(regionSearchRequestDto);
		
		return ResponseEntity.ok(new SuccessResponse<>("local2 議고쉶 ?깃났", result));
	}
	
	@GetMapping("local/{local1}/{local2}")
	public ResponseEntity<?> selectLocal3(@PathVariable String local1 , @PathVariable String local2){
		RegionSearchRequestDto regionSearchRequestDto = new RegionSearchRequestDto();
		regionSearchRequestDto.setLocal1(local1);
		regionSearchRequestDto.setLocal2(local2);
		
		RegionSearchResponseDto result = propertySearchService.selectLocal3(regionSearchRequestDto);
		
		return ResponseEntity.ok(new SuccessResponse<>("local3 議고쉶 ?깃났", result));
	}
	
	@GetMapping("recent")
	public ResponseEntity<?> selectRecentProperty(@ModelAttribute PageRequestDto pageDto){
		PropertyDetailListResponseDto result = propertySearchService.selectRecentProperty(pageDto);
		
		return ResponseEntity.ok(new SuccessResponse<>("留ㅻЪ 議고쉶 ?깃났", result));
	}
	
	@GetMapping("count/all")
	public ResponseEntity<?> countAll(){
		return ResponseEntity.ok(new SuccessResponse<>("留ㅻЪ ?꾩껜??諛섑솚 ?깃났", propertySearchService.countAll()));
	}
}
