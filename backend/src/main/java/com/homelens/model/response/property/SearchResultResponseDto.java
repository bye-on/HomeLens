package com.homelens.model.response.property;

import lombok.Data;
import java.util.List;

@Data
public class SearchResultResponseDto {

    // 1) 임베딩 + 하드 필터 기반 TopK
    private List<PropertyDetailResponseDto> primary;

    // 2) 지역(local1/2/3) 기반 일반 리스트
    private List<PropertyDetailResponseDto> regionOnly;
}
