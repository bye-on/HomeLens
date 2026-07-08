package com.homelens.filter.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.homelens.model.CustomUserDetails;
import com.homelens.model.service.user.CustomUserDetailsService;
import com.homelens.model.service.user.CustomUserDetailsServiceImpl;
import com.homelens.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTVerificationFilter extends OncePerRequestFilter{
	private final JwtUtil jwtUtil;
	private final CustomUserDetailsServiceImpl userDetailService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = extractToken(request);
		
		if(token == null) {
			filterChain.doFilter(request, response);
			return;
		}
		
		Claims claims = jwtUtil.getClaims(token);
		String email = claims.get("userEmail").toString();
		CustomUserDetails details = (CustomUserDetails)userDetailService.loadUserByUsername(email);
		
		request.setAttribute("userId", details.getId());
		
		Authentication authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        
        filterChain.doFilter(request, response);
	}
	
	private String extractToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
    	if(token != null && token.startsWith("Bearer ")) {
    		return token.substring(7);
    	}
    	
         return null;
        // END
	}

}
