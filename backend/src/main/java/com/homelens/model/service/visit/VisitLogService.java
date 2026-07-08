package com.homelens.model.service.visit;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homelens.mapper.VisitMapper;
import com.homelens.model.request.visit.VisitRequestDto;
import com.homelens.model.response.visit.DailyVisitCountRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitLogService {

    private final VisitMapper visitMapper;

    @Transactional
    public boolean recordDailyVisit(String visitorKey, Long userId) {
    	VisitRequestDto param = new VisitRequestDto(visitorKey, userId);
        int affected = visitMapper.insertDailyVisit(param);
        // inserted면 1, 이미 있으면 0
        return affected == 1;
    }

    @Transactional(readOnly = true)
    public Long getDailyVisitors(int days) {
        LocalDate fromDay = LocalDate.now().minusDays(days - 1L);
        List<DailyVisitCountRequestDto> list =  visitMapper.selectDailyVisitors(fromDay);
        Long sum = 0L;
        
        for(DailyVisitCountRequestDto daily : list) {
        	sum += daily.getVisitors();
        }
        
        return sum;
    }
}

