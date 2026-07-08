package com.homelens.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.homelens.model.CustomUserDetails;
import com.homelens.model.response.user.UserResponseDto;
import com.homelens.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authorization = request.getHeader("Authorization");

		if (authorization == null || !authorization.startsWith("Bearer ")) {
			System.out.println("token null");
			filterChain.doFilter(request, response);

			return;
		}

		String token = authorization.split(" ")[1];
		
		System.out.println("auth header=" + authorization);
		System.out.println("token len=" + token.length());


		if (jwtUtil.isExpired(token)) {
			System.out.println("token expired");
			filterChain.doFilter(request, response);

			return;
		}

		String email = jwtUtil.getEmail(token);
		String username = jwtUtil.getUsername(token);
		String role = jwtUtil.getRole(token);

		UserResponseDto userInfoDto = new UserResponseDto();
		userInfoDto.setUserEmail(email);
		userInfoDto.setUserName(username);
		userInfoDto.setUserPw("temppassword");
		userInfoDto.setUserRole(role);
		
		System.out.println("token role = " + role);
		System.out.println("dto role   = " + userInfoDto.getUserRole());

		CustomUserDetails customUserDetails = new CustomUserDetails(userInfoDto);

		Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails,
	            null,
	            customUserDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);
	}

}
