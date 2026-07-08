package com.homelens.model.service.board;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.mapper.BoardMapper;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.response.board.BoardDetailResponseDto;
import com.homelens.model.response.board.BoardListResponseDto;
import com.homelens.model.response.board.BoardResponseDto;
import com.homelens.model.response.board.WriteBoardRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;

    @Override
    public BoardListResponseDto selectAll(PageRequestDto pageDto) {
    	int count = boardMapper.countBoard();
    	int totalPage = (count / pageDto.getSize()) + 1;
    	int currentPage = pageDto.getPage();
    	
    	if(currentPage > totalPage)
    		currentPage = totalPage;
    	
        List<BoardResponseDto> list = boardMapper.selectAll(pageDto);
        if (list == null) {
            throw new CustomException(ErrorCode.NOT_FOUND, "게시물을 찾을 수 없습니다.");
        }
        if(list.isEmpty() ) {
        	throw new CustomException(ErrorCode.NOT_FOUND, "게시물이 없습니다!");
        }
        BoardListResponseDto boardList = new BoardListResponseDto(list, totalPage, currentPage);
        
        return boardList;
    }

    @Override
    public BoardDetailResponseDto selectById(int boardId) {
    	BoardDetailResponseDto dto = boardMapper.selectById(boardId);
        if (dto == null) {
            throw new CustomException(ErrorCode.NOT_FOUND, "게시물을 찾을 수 없습니다..");
        }
        return dto;
    }

    @Override
    @Transactional
    public int write(WriteBoardRequestDto boardDto) {
        int result = boardMapper.write(boardDto);
        if (result == 0) {
            throw new CustomException(ErrorCode.BOARD_CREATE_FAILED, "글 생성 오류입니다.");
        }
        return result;
    }

    @Override
    @Transactional
    public int edit(int boardId, WriteBoardRequestDto boardDto) {
        int result = boardMapper.edit(boardId, boardDto);
        if (result == 0) {
            throw new CustomException(ErrorCode.BOARD_UPDATE_FAILED, "글 수정 오류입니다.");
        }
        return result;
    }

    @Override
    @Transactional
    public int delete(int boardId) {
        int result = boardMapper.delete(boardId);
        if (result == 0) {
            throw new CustomException(ErrorCode.BOARD_DELETE_FAILED, "글 삭제 오류입니다.");
        }
        return result;
    }
    
    
    public int countAll() {
    	return boardMapper.countBoard();
    }
}
