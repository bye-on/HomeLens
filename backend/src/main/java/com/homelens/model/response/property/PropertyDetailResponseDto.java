package com.homelens.model.response.property;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDetailResponseDto {

    private Long itemId;

    private String title;
    private String salesType;     // 매매 / 전세 / 월세
    private String serviceType;   // 중개 / 직거래 등

    private String local1;
    private String local2;
    private String local3;
    private String jibunAddress;

    private int deposit;
    private int rent;
    private float manageCost;
    private String manageNotIncludes;

    private String options;

    private Integer areaM2;
    private Integer areaContractM2;

    private String floorLevel;
    private Integer allFloors;

    // 위치
    private Double lat;
    private Double lng;

    // JSON 컬럼
    private JsonNode subway;
    private JsonNode nbDistribution;
    private JsonNode nbAmenity;
    private JsonNode nbPoi;
    private JsonNode itemImage;

    // 관심매물 정보
    private int likeCount;
    private boolean userLike;
    private boolean sold;
}
