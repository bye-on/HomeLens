package com.homelens.model.response.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MapCoordinateResponseDto {
    private Long itemId;
    private Double lat;
    private Double lng;
}
