package com.homelens.model.response.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BoardDetailResponseDto {
	@Schema(description = "글 ID")
	private int boardId;
	
	@Schema(description = "글 제목")
	private String subject;
	
	@Schema(description = "글 내용")
	private String content;
	
	@Schema(description = "글 작성자 ID")
	private int publisherId;
	
	@Schema(description = "글 작성자 NAME")
	private String userName;
	
	@Schema(description = "글 작성 시간")
	private String registerTime;
}
