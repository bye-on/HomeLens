package com.homelens.model.response.fastApi;

import lombok.Data;
import java.util.List;

import com.homelens.model.FastApiFilterDto;

@Data
public class FastApiResponseDto {
    private String embedding_text;
    private List<Double> embedding_vector;
    private FastApiFilterDto filters;
}
