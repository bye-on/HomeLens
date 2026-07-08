package com.homelens.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.homelens.model.request.PageRequestDto;
import com.homelens.model.response.board.BoardDetailResponseDto;
import com.homelens.model.response.board.BoardResponseDto;
import com.homelens.model.response.board.WriteBoardRequestDto;

@Mapper
public interface BoardMapper {
	List<BoardResponseDto> selectAll(PageRequestDto pageDto);
	int countBoard();
	BoardDetailResponseDto selectById(int boardId);
	int write(WriteBoardRequestDto boardDto);
	int edit(@Param("boardId") int boardId, @Param("boardDto") WriteBoardRequestDto boardDto);
	int delete(int boardId);
}
