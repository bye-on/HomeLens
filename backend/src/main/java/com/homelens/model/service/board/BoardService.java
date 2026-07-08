package com.homelens.model.service.board;

import com.homelens.model.request.PageRequestDto;
import com.homelens.model.response.board.BoardDetailResponseDto;
import com.homelens.model.response.board.BoardListResponseDto;
import com.homelens.model.response.board.BoardResponseDto;
import com.homelens.model.response.board.WriteBoardRequestDto;

public interface BoardService {
	BoardListResponseDto selectAll(PageRequestDto pageDto);
	BoardDetailResponseDto selectById(int boardId);
	int write(WriteBoardRequestDto boardDto);
	int edit(int boardId, WriteBoardRequestDto boardDto);
	int delete(int boardId);
	public int countAll();
}
