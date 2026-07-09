package com.homelens.model.request.property;

import lombok.Data;

@Data
public class MapSearchRequestDto {
    private Double swLat;
    private Double swLng;
    private Double neLat;
    private Double neLng;
    private String local1;
    private String local2;
    private String local3;
    private Integer size = 1000;

    public Integer getLimit() {
        if (size == null || size < 1) return 1000;
        return Math.min(size, 2000);
    }
}
