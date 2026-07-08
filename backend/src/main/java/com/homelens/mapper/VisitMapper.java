package com.homelens.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.homelens.model.request.visit.VisitRequestDto;
import com.homelens.model.response.visit.DailyVisitCountRequestDto;

@Mapper
public interface VisitMapper {

    // 오늘 방문 1회 기록(이미 있으면 무시)
    int insertDailyVisit(VisitRequestDto param);

    // 일별 방문자 수 조회 (최근 N일)
    List<DailyVisitCountRequestDto> selectDailyVisitors(@Param("fromDay") LocalDate fromDay);
}
