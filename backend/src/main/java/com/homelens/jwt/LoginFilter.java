package com.homelens.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homelens.common.ErrorResponse;
import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.model.CustomUserDetails;
import com.homelens.model.request.user.LoginRequestDto;
import com.homelens.model.response.user.UserResponseDto;
import com.homelens.model.service.user.UserService;
import com.homelens.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

//	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public LoginFilter(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
		super(authenticationManager);
		this.userService = userService;
		this.jwtUtil = jwtUtil;

		setFilterProcessesUrl("/api/v1/auth/login");
		setUsernameParameter("email");
		setPasswordParameter("password");
		
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		LoginRequestDto loginDTO = new LoginRequestDto();

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ServletInputStream inputStream = request.getInputStream();
			String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
			loginDTO = objectMapper.readValue(messageBody, LoginRequestDto.class);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String email = loginDTO.getEmail();
		String password = loginDTO.getPassword();

		log.debug("Login attempt for {}", email);

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
		
		return this.getAuthenticationManager().authenticate(authToken);

	}

	// 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) {

		Map<String, Object> result = new HashMap<>();
		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

		String token = jwtUtil.createAccessToken(customUserDetails.getUserDto());
		result.put("accessToken", token);
		
		Map<String, Object> claims = jwtUtil.getClaims(token.split(" ")[1]);
		
		System.out.println("claims 리스트@@@@@@@@@@@@@@@@");
		for(Map.Entry<String, Object> e : claims.entrySet()) {
			System.out.println(e.getKey() + " : " + e.getValue());
		}
		
		UserResponseDto userDto = customUserDetails.getUserDto();
		String refresh = jwtUtil.createRefreshToken(userDto);
		result.put("refreshToken", refresh);
		result.put("userName", userDto.getUserName());

		userDto.setUserRefreshToken(refresh);
		userService.updateUserRefreshToken(userDto);

		handleResult(response, result, HttpStatus.OK);

		response.addHeader("Authorization", token);
	}

	// 로그인 실패시 실행하는 메소드
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws JsonProcessingException, IOException {
		ErrorResponse error;

        if (exception instanceof DisabledException) {
            error = new ErrorResponse(
                    403,
                    "FORBIDDEN",
                    "이메일 인증이 완료되지 않았거나 비활성화된 계정입니다."
            );
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else if (exception instanceof BadCredentialsException) {
            error = new ErrorResponse(
                    401,
                    "UNAUTHORIZED",
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            error = new ErrorResponse(
                    401,
                    "AUTH_FAILED",
                    "로그인에 실패했습니다."
            );
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(error));
	}

	private void handleResult(HttpServletResponse response, Map<String, ?> data, HttpStatus status) {
		response.setContentType("application/json;charset=UTF-8");
		try {
			String jsonResponse = new ObjectMapper().writeValueAsString(data);
			response.setStatus(status.value());
			response.getWriter().write(jsonResponse);
		} catch (IOException e) {
			log.error("Error writing JSON response", e);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
}
