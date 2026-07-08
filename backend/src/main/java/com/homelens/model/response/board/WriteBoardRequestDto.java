package com.homelens.model.response.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "WriteBoardDto : 게시글 목록 정보", description = "게시글 수정과 삭제를 위한 정보이다.")
public class WriteBoardRequestDto {
	@Schema(description = "글 작성자 ID")
	private int publisherId;
	
	@Schema(description = "글 제목")
	private String subject;
	
	@Schema(description = "글 내용")
	private String content;
}
