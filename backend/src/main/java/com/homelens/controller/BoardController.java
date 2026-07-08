package com.homelens.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homelens.common.SuccessResponse;
import com.homelens.config.SecurityConfig;
import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.model.CustomUserDetails;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.response.board.BoardDetailResponseDto;
import com.homelens.model.response.board.BoardListResponseDto;
import com.homelens.model.response.board.BoardResponseDto;
import com.homelens.model.response.board.WriteBoardRequestDto;
import com.homelens.model.service.board.BoardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
@Tag(name = "Board Controller", description = "게시판 CRUD 전체 처리 컨트롤러")
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "게시판 글목록", description = "모든 게시글의 정보를 반환한다.")
    @GetMapping("/list")
    public ResponseEntity<SuccessResponse<BoardListResponseDto>> selectAll(
    		@ModelAttribute PageRequestDto pageDto) {
    	BoardListResponseDto list = boardService.selectAll(pageDto);
        
        return ResponseEntity.ok(
            new SuccessResponse<>("글 목록 조회 성공", list)
        );
    }

    @Operation(summary = "게시판 글보기", description = "글번호에 해당하는 게시글의 정보를 반환한다.")
    @GetMapping("/view/{boardId}")
    public ResponseEntity<SuccessResponse<BoardDetailResponseDto>> selectById(@PathVariable int boardId) {
    	BoardDetailResponseDto board = boardService.selectById(boardId);

        return ResponseEntity.ok(
            new SuccessResponse<>("게시글 조회 성공", board)
        );
    }

    @Operation(summary = "게시판 글작성", description = "새로운 게시글을 등록한다.")
    @PostMapping("/write")
    public ResponseEntity<SuccessResponse<Integer>> write(
    		Authentication authentication,
            @RequestBody WriteBoardRequestDto writeBoardDto
    ) {
    	if(authentication == null) {
    		throw new CustomException(ErrorCode.USER_UNAUTHORIZED, "로그인이 필요합니다.");
    	}

        int userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        
        writeBoardDto.setPublisherId(userId);  // 작성자 설정

        int result = boardService.write(writeBoardDto);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new SuccessResponse<>("게시글 등록 성공", result));
    }

    @Operation(summary = "게시판 글수정", description = "게시글 내용을 수정한다")
    @PutMapping("/edit/{boardId}")
    public ResponseEntity<SuccessResponse<Integer>> edit(
            @PathVariable int boardId,
            @RequestBody WriteBoardRequestDto writeBoardDto
    ) {
        int result = boardService.edit(boardId, writeBoardDto);

        return ResponseEntity.ok(
            new SuccessResponse<>("게시글 수정 성공", result)
        );
    }

    @Operation(summary = "게시판 글삭제", description = "게시글을 삭제한다")
    @DeleteMapping("/delete/{boardId}")
    public ResponseEntity<SuccessResponse<Integer>> delete(@PathVariable int boardId) {
        int result = boardService.delete(boardId);

        return ResponseEntity.ok(
            new SuccessResponse<>("게시글 삭제 성공", result)
        );
    }
    
    @GetMapping("/count/all")
    public ResponseEntity<?> selectAll(){
    	return ResponseEntity.ok(new SuccessResponse<>("게시글 개수", boardService.countAll()));
    }

}
