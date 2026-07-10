package com.homelens.model.response.property;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapItemCursorResponseDto {
    private List<PropertyDetailResponseDto> items;
    private Long nextCursor;
    private boolean hasNext;
}
