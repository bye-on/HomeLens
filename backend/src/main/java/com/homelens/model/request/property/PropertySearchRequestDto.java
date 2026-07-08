package com.homelens.model.request.property;

import java.util.List;

import lombok.Data;

@Data
public class PropertySearchRequestDto {
    // pgvector??踰≫꽣 由ы꽣?? "[0.1234, -0.5678, ...]"
    private String embeddingLiteral;

    // ?섎뱶 ?꾪꽣
    private String salesType;
    private String serviceType;
    private String local1;
    private String local2;
    private String local3;

    private Integer depositMin;
    private Integer depositMax;
    private Integer rentMax;
    private Integer areaMin;
    private Integer areaMax;
    private Integer manageCostMax;

    // ?섏씠吏?由щ컠
    private Integer topK;        // ?꾨쿋??湲곕컲 ?곸쐞 N媛?
    private Integer regionLimit; // 吏??湲곕컲 由ъ뒪??媛쒖닔
    
    private List<Long> primaryList;
    private List<String> keywords;
}