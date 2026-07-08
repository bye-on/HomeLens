package com.homelens.model.response.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "BoardDto : 게시글 목록 정보", description = "게시글 목록 정보를 나타낸다.")
public class BoardResponseDto {
	@Schema(description = "글 ID")
	private int boardId;
	
	@Schema(description = "글 제목")
	private String subject;
	
	@Schema(description = "글 작성자 ID")
	private int publisherId;
	
	@Schema(description = "글 작성자 NAME")
	private String userName;
	
	@Schema(description = "글 작성 시간")
	private String registerTime;
}
