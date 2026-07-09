package com.homelens.model.service.property;

import com.homelens.model.*;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.property.MapSearchRequestDto;
import com.homelens.model.request.property.PropertySearchByIdRequestDto;
import com.homelens.model.request.property.PropertySearchRequestDto;
import com.homelens.model.request.property.RegionSearchRequestDto;
import com.homelens.model.request.property.SearchRequestDto;
import com.homelens.model.response.board.BoardListResponseDto;
import com.homelens.model.response.board.BoardResponseDto;
import com.homelens.model.response.fastApi.FastApiResponseDto;
import com.homelens.model.response.property.PropertyDetailListResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;
import com.homelens.model.response.property.RegionSearchResponseDto;
import com.homelens.model.response.property.SearchResultResponseDto;
import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertySearchServiceImpl implements PropertySearchService {

	private final WebClient fastApiWebClient;
	private final PropertyMapper propertyMapper;
	
	public SearchResultResponseDto propertySearch(SearchRequestDto req) {
		int topK = (req.getTopK() != null) ? req.getTopK() : 20;

		// 1) FastAPI ?몄텧?댁꽌 embedding + filters 諛쏄린
		FastApiResponseDto faResp = fastApiWebClient.post().uri("/semantic/prepare")
				.bodyValue(Map.of("query", req.getQuery(), "top_k", topK)).retrieve()
				.bodyToMono(FastApiResponseDto.class).block(); // 媛꾨떒??block (?먰븯硫?鍮꾨룞湲곕줈 媛쒖꽑 媛??

		FastApiFilterDto f = faResp.getFilters();
		
		boolean invalid = faResp.getEmbedding_text().startsWith("?곸젅??遺?숈궛 寃??吏덉쓽媛 ?꾨떃?덈떎");
		
		if(invalid) {
			throw new CustomException(ErrorCode.INVALID_QUERY, "?곸젅??遺?숈궛 寃??吏덉쓽媛 ?꾨떃?덈떎.");
		}
		
		faResp = fastApiWebClient.post().uri("/semantic/embed")
				.bodyValue(Map.of("embedding_text", faResp.getEmbedding_text())).retrieve()
				.bodyToMono(FastApiResponseDto.class).block(); // 媛꾨떒??block (?먰븯硫?鍮꾨룞湲곕줈 媛쒖꽑 媛??

		// 2) embedding_vector -> pgvector ?띿뒪??由ы꽣??"[1.23456789,0.1234,...]"
		String embeddingLiteral = faResp.getEmbedding_vector().stream().map(v -> String.format(Locale.ROOT, "%.8f", v))
				.collect(Collectors.joining(",", "[", "]"));

		// 3) MyBatis???뚮씪誘명꽣 ?명똿
		PropertySearchRequestDto query = new PropertySearchRequestDto();
		query.setEmbeddingLiteral(embeddingLiteral);

		query.setSalesType(f.getSales_type());
		query.setServiceType(f.getService_type());
		query.setLocal1(f.getLocal1());
		query.setLocal2(f.getLocal2());
		query.setLocal3(f.getLocal3());

		query.setDepositMin(f.getDeposit_min());
		query.setDepositMax(f.getDeposit_max());
		query.setRentMax(f.getRent_max());
		query.setAreaMin(f.getArea_m2_min());
		query.setAreaMax(f.getArea_m2_max());
		query.setManageCostMax(f.getManage_cost_max());

		query.setTopK(topK);
		query.setRegionLimit(100); // 吏??由ъ뒪?몃뒗 100媛??뺣룄 (?먰븯?붾?濡?議곗젙)

		// 4) 荑쇰━ 2媛??ㅽ뻾
		List<PropertyDetailResponseDto> primary = propertyMapper.selectBySemanticFilters(query);
		List<Long> primaryList = new ArrayList<>();
		for(PropertyDetailResponseDto psrd: primary) {
			primaryList.add(psrd.getItemId());
		}
		query.setPrimaryList(primaryList);
		List<PropertyDetailResponseDto> regionOnly = propertyMapper.selectByRegion(query);

		// 5) ?묐떟 ?⑹튂湲?
		SearchResultResponseDto result = new SearchResultResponseDto();
		result.setPrimary(primary);
		result.setRegionOnly(regionOnly);
		return result;
	}

	@Override
	public SearchResultResponseDto propertyBasicSearch(SearchRequestDto req) {
		int topK = (req.getTopK() != null) ? req.getTopK() : 50;

		String rawQuery = (req.getQuery() == null) ? "" : req.getQuery().trim();
		List<String> keywords = Arrays.stream(rawQuery.split("\\s+"))
				.map(String::trim)
				.filter(keyword -> !keyword.isBlank())
				.distinct()
				.collect(Collectors.toList());

		PropertySearchRequestDto query = new PropertySearchRequestDto();
		query.setTopK(topK);
		query.setKeywords(keywords);

		SearchResultResponseDto result = new SearchResultResponseDto();
		result.setPrimary(propertyMapper.selectByBasicSearch(query));
		result.setRegionOnly(Collections.emptyList());
		return result;
	}

	@Override
	public List<PropertyDetailResponseDto> selectMapProperties(MapSearchRequestDto req) {
		return propertyMapper.selectMapProperties(req);
	}

	@Override
	public PropertyDetailResponseDto selectPropertyByItemId(PropertySearchByIdRequestDto propertySearchByIdRequestDto) {
		PropertyDetailResponseDto propertyDetailResponseDto = null;
		propertyDetailResponseDto = propertyMapper.selectPropertyByItemId(propertySearchByIdRequestDto);
		
		if(propertyDetailResponseDto == null) {
			throw new CustomException(ErrorCode.PROPERTY_NOT_FOUND, "?붿껌?섏떊 遺?숈궛 留ㅻЪ??李얠쓣 ???놁뒿?덈떎.");
		}
		
		return propertyDetailResponseDto;
	}

	@Override
	public RegionSearchResponseDto selectLocal1(RegionSearchRequestDto regionSearchRequestDto) {
		RegionSearchResponseDto regionSearchResponseDto = new RegionSearchResponseDto();
		regionSearchResponseDto.setLocal(propertyMapper.selectLocal1(regionSearchRequestDto));
		
		return regionSearchResponseDto;
	}

	@Override
	public RegionSearchResponseDto selectLocal2(RegionSearchRequestDto regionSearchRequestDto) {
		RegionSearchResponseDto regionSearchResponseDto = new RegionSearchResponseDto();
		regionSearchResponseDto.setLocal(propertyMapper.selectLocal2(regionSearchRequestDto));
		
		if(regionSearchResponseDto.getLocal() == null || regionSearchResponseDto.getLocal().isEmpty()) {
			throw new CustomException(ErrorCode.REGION_NOT_FOUND, "?대떦 吏???곗씠?곌? ?놁뒿?덈떎.");
		}
		
		return regionSearchResponseDto;
	}

	@Override
	public RegionSearchResponseDto selectLocal3(RegionSearchRequestDto regionSearchRequestDto) {
		RegionSearchResponseDto regionSearchResponseDto = new RegionSearchResponseDto();
		regionSearchResponseDto.setLocal(propertyMapper.selectLocal3(regionSearchRequestDto));
		
		if(regionSearchResponseDto.getLocal() == null || regionSearchResponseDto.getLocal().isEmpty()) {
			throw new CustomException(ErrorCode.REGION_NOT_FOUND, "?대떦 吏???곗씠?곌? ?놁뒿?덈떎.");
		}
		
		return regionSearchResponseDto;
	}

	@Override
	public PropertyDetailListResponseDto selectRecentProperty(PageRequestDto pageDto) {
		
		PropertyDetailListResponseDto result = new PropertyDetailListResponseDto();
		
		int count = propertyMapper.countProperty();
    	int totalPage = (count / pageDto.getSize()) + 1;
    	int currentPage = pageDto.getPage();
    	
    	System.out.println(totalPage);
    	System.out.println(currentPage);
    	
    	if(currentPage > totalPage)
    		currentPage = totalPage;
    	
        List<PropertyDetailResponseDto> list = propertyMapper.selectRecentProperty(pageDto);
        
        if (list == null) {
            throw new CustomException(ErrorCode.PROPERTY_NOT_FOUND, "留ㅻЪ??李얠쓣 ???놁뒿?덈떎.");
        }
        if(list.isEmpty()) {
        	throw new CustomException(ErrorCode.NOT_FOUND, "留ㅻЪ???놁뒿?덈떎!");
        }
        
        result.setRecentProperty(list);
        result.setCurrentPage(currentPage);
        result.setTotalPage(totalPage);
		
		return result;
	}
	
	public Integer countAll() {
		return propertyMapper.countProperty();
	}
	
	
}
