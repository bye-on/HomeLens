package com.homelens.model.response.board;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoardListResponseDto {
	List<BoardResponseDto> list;
	int totalPage;
	int currentPage;
}
