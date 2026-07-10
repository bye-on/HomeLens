package com.homelens.model.service.property;

import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.mapper.PropertyMapper;
import com.homelens.model.FastApiFilterDto;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.property.MapSearchRequestDto;
import com.homelens.model.request.property.PropertySearchByIdRequestDto;
import com.homelens.model.request.property.PropertySearchRequestDto;
import com.homelens.model.request.property.RegionSearchRequestDto;
import com.homelens.model.request.property.SearchRequestDto;
import com.homelens.model.response.fastApi.FastApiResponseDto;
import com.homelens.model.response.property.MapCoordinateResponseDto;
import com.homelens.model.response.property.MapItemCursorResponseDto;
import com.homelens.model.response.property.PropertyDetailListResponseDto;
import com.homelens.model.response.property.PropertyDetailResponseDto;
import com.homelens.model.response.property.RegionSearchResponseDto;
import com.homelens.model.response.property.SearchResultResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertySearchServiceImpl implements PropertySearchService {

    private final WebClient fastApiWebClient;
    private final PropertyMapper propertyMapper;

    @Override
    public SearchResultResponseDto propertySearch(SearchRequestDto req) {
        int topK = (req.getTopK() != null) ? req.getTopK() : 20;

        // 1) FastAPI에서 검색 조건과 임베딩에 사용할 문장을 생성한다.
        FastApiResponseDto faResp = fastApiWebClient.post()
                .uri("/semantic/prepare")
                .bodyValue(Map.of("query", req.getQuery(), "top_k", topK))
                .retrieve()
                .bodyToMono(FastApiResponseDto.class)
                .block();

        FastApiFilterDto filters = faResp.getFilters();

        boolean invalid = faResp.getEmbedding_text()
                .startsWith("적절한 부동산 검색 질의가 아닙니다");

        if (invalid) {
            throw new CustomException(
                    ErrorCode.INVALID_QUERY,
                    "적절한 부동산 검색 질의가 아닙니다."
            );
        }

        faResp = fastApiWebClient.post()
                .uri("/semantic/embed")
                .bodyValue(Map.of("embedding_text", faResp.getEmbedding_text()))
                .retrieve()
                .bodyToMono(FastApiResponseDto.class)
                .block();

        // 2) 임베딩 벡터를 pgvector가 인식하는 문자열 형식으로 변환한다.
        // 예: [1.23456789,0.12340000,...]
        String embeddingLiteral = faResp.getEmbedding_vector().stream()
                .map(value -> String.format(Locale.ROOT, "%.8f", value))
                .collect(Collectors.joining(",", "[", "]"));

        // 3) FastAPI에서 추출한 검색 조건을 MyBatis 요청 객체에 설정한다.
        PropertySearchRequestDto query = new PropertySearchRequestDto();
        query.setEmbeddingLiteral(embeddingLiteral);
        query.setSalesType(filters.getSales_type());
        query.setServiceType(filters.getService_type());
        query.setLocal1(filters.getLocal1());
        query.setLocal2(filters.getLocal2());
        query.setLocal3(filters.getLocal3());
        query.setDepositMin(filters.getDeposit_min());
        query.setDepositMax(filters.getDeposit_max());
        query.setRentMax(filters.getRent_max());
        query.setAreaMin(filters.getArea_m2_min());
        query.setAreaMax(filters.getArea_m2_max());
        query.setManageCostMax(filters.getManage_cost_max());
        query.setTopK(topK);
        query.setRegionLimit(100);

        // 4) 조건과 의미가 일치하는 매물을 우선 조회하고, 동일 지역의 대체 매물을 추가한다.
        List<PropertyDetailResponseDto> primary = propertyMapper.selectBySemanticFilters(query);
        List<Long> primaryIds = new ArrayList<>();
        for (PropertyDetailResponseDto property : primary) {
            primaryIds.add(property.getItemId());
        }

        query.setPrimaryList(primaryIds);
        List<PropertyDetailResponseDto> regionOnly = propertyMapper.selectByRegion(query);

        // 5) 우선 검색 결과와 지역 대체 결과를 함께 반환한다.
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
    public List<MapCoordinateResponseDto> selectMapProperties(MapSearchRequestDto req) {
        return propertyMapper.selectMapProperties(req);
    }

    @Override
    public MapItemCursorResponseDto selectMapPropertyItems(MapSearchRequestDto req) {
        List<PropertyDetailResponseDto> rows = propertyMapper.selectMapPropertyItems(req);
        int pageSize = req.getPageSize();
        boolean hasNext = rows.size() > pageSize;
        List<PropertyDetailResponseDto> items = hasNext ? rows.subList(0, pageSize) : rows;
        Long nextCursor = hasNext && !items.isEmpty()
                ? items.get(items.size() - 1).getItemId()
                : null;

        return MapItemCursorResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Override
    public PropertyDetailResponseDto selectPropertyByItemId(
            PropertySearchByIdRequestDto propertySearchByIdRequestDto
    ) {
        PropertyDetailResponseDto property =
                propertyMapper.selectPropertyByItemId(propertySearchByIdRequestDto);

        if (property == null) {
            throw new CustomException(
                    ErrorCode.PROPERTY_NOT_FOUND,
                    "요청하신 부동산 매물을 찾을 수 없습니다."
            );
        }

        return property;
    }

    @Override
    public RegionSearchResponseDto selectLocal1(RegionSearchRequestDto request) {
        RegionSearchResponseDto response = new RegionSearchResponseDto();
        response.setLocal(propertyMapper.selectLocal1(request));
        return response;
    }

    @Override
    public RegionSearchResponseDto selectLocal2(RegionSearchRequestDto request) {
        RegionSearchResponseDto response = new RegionSearchResponseDto();
        response.setLocal(propertyMapper.selectLocal2(request));

        if (response.getLocal() == null || response.getLocal().isEmpty()) {
            throw new CustomException(
                    ErrorCode.REGION_NOT_FOUND,
                    "해당 지역 정보를 찾을 수 없습니다."
            );
        }

        return response;
    }

    @Override
    public RegionSearchResponseDto selectLocal3(RegionSearchRequestDto request) {
        RegionSearchResponseDto response = new RegionSearchResponseDto();
        response.setLocal(propertyMapper.selectLocal3(request));

        if (response.getLocal() == null || response.getLocal().isEmpty()) {
            throw new CustomException(
                    ErrorCode.REGION_NOT_FOUND,
                    "해당 지역 정보를 찾을 수 없습니다."
            );
        }

        return response;
    }

    @Override
    public PropertyDetailListResponseDto selectRecentProperty(PageRequestDto pageDto) {
        PropertyDetailListResponseDto result = new PropertyDetailListResponseDto();

        int count = propertyMapper.countProperty();
        int totalPage = (count / pageDto.getSize()) + 1;
        int currentPage = pageDto.getPage();

        if (currentPage > totalPage) {
            currentPage = totalPage;
        }

        List<PropertyDetailResponseDto> list = propertyMapper.selectRecentProperty(pageDto);

        if (list == null) {
            throw new CustomException(
                    ErrorCode.PROPERTY_NOT_FOUND,
                    "최근 등록 매물을 찾을 수 없습니다."
            );
        }
        if (list.isEmpty()) {
            throw new CustomException(
                    ErrorCode.NOT_FOUND,
                    "최근 등록 매물이 없습니다."
            );
        }

        result.setRecentProperty(list);
        result.setCurrentPage(currentPage);
        result.setTotalPage(totalPage);
        return result;
    }

    @Override
    public Integer countAll() {
        return propertyMapper.countProperty();
    }
}
