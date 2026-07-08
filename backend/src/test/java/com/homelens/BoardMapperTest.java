//package com.homelens;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.homelens.mapper.BoardMapper;
//import com.homelens.model.BoardDto;
//
//import lombok.extern.slf4j.Slf4j;
//
//@SpringBootTest
//@Transactional
//@Slf4j
//class BoardMapperTest {
//
//    @Autowired
//    private BoardMapper boardMapper;
//
//    @Test
//    void testSelectAll() {
//        List<BoardDto> list = boardMapper.selectAll();
//        System.out.println(list);
//    }
//
//    @Test
//    void testSelectById() {
//        BoardDto board = boardMapper.selectById(1);
//        System.out.println(board);
//    }
//
//    @Test
//    void testWrite() {
//        BoardDto dto = new BoardDto();
//        dto.setPublisherId(1);
//        dto.setSubject("테스트 제목");
//        dto.setContent("테스트 내용입니다.");
//
//        int result = boardMapper.write(dto);
//        log.info("Insert result = " + result);
//
//        assertEquals(1, result);
//    }
//    
//    @Test
//    void testEdit() {
//        BoardDto dto = new BoardDto();
//        dto.setBoardId(2);   // 수정할 게시글 ID
//        dto.setSubject("수정된 제목");
//        dto.setContent("수정된 내용");
//
//        int result = boardMapper.edit(dto);
//        log.info("Edit result = " + result);
//
//        assertEquals(1, result);
//    }
//
//    @Test
//    void testDelete() {
//        int boardId = 2; // 삭제할 게시글 ID
//
//        int result = boardMapper.delete(boardId);
//        log.info("Delete result = " + result);
//
//        assertEquals(1, result);
//    }
//}
