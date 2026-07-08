package com.homelens.model.request.property;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SearchRequestDto {
    private String query;   // ?ъ슜?먭? ?먯뿰?대줈 ?낅젰??寃??臾몄옣
    private String mode;
    @JsonProperty("top_k")
    private Integer topK;   // ?곸쐞 紐?媛쒓퉴吏 ?꾨쿋??湲곕컲?쇰줈 媛?몄삱吏 (湲곕낯 20)
}