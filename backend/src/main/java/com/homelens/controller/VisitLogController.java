package com.homelens.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.homelens.model.response.visit.DailyVisitCountRequestDto;
import com.homelens.model.service.visit.VisitLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/visit")
@RequiredArgsConstructor
public class VisitLogController {

    private static final String VISITOR_COOKIE = "visitor_id";
    private static final int COOKIE_MAX_AGE_SEC = 60 * 60 * 24 * 365; // 1년

    private final VisitLogService visitLogService;

    /**
     * 방문 기록(하루 1회)
     * - 로그인: userId가 있으면 "u:{userId}"
     * - 비로그인: 쿠키 UUID로 "c:{uuid}"
     */
    @PostMapping("/ping")
    public ResponseEntity<?> ping(HttpServletRequest request, HttpServletResponse response) {
    	Object v = request.getAttribute("userId");
    	Long userId = null;
    	if (v instanceof Number) {
    	    userId = ((Number) v).longValue();
    	}

        String visitorKey = resolveVisitorKey(request, response, userId);
        boolean inserted = visitLogService.recordDailyVisit(visitorKey, userId);

        return ResponseEntity.ok(
            java.util.Map.of(
                "ok", true,
                "visitorKey", visitorKey,
                "countedToday", inserted
            )
        );
    }

    /**
     * 최근 N일 방문자 수 조회
     */
    @GetMapping("/daily")
    public ResponseEntity<?> daily(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(java.util.Map.of("visitors", visitLogService.getDailyVisitors(days)));
    }

    private String resolveVisitorKey(HttpServletRequest request, HttpServletResponse response, Long userId) {
        // 로그인 유저면 가장 정확하게 userId로 식별
        if (userId != null && userId > 0) {
            return "u:" + userId;
        }

        // 비로그인은 쿠키 UUID 사용
        String cookieVal = getCookieValue(request, VISITOR_COOKIE);
        if (cookieVal == null || cookieVal.isBlank()) {
            cookieVal = UUID.randomUUID().toString();
            Cookie c = new Cookie(VISITOR_COOKIE, cookieVal);
            c.setHttpOnly(true);
            c.setPath("/");
            c.setMaxAge(COOKIE_MAX_AGE_SEC);
            // HTTPS 쓰면 true 권장
            // c.setSecure(true);
            response.addCookie(c);
        }
        return "c:" + cookieVal;
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}

