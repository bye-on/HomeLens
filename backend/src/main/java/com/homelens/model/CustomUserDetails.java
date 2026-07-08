package com.homelens.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.model.response.user.UserResponseDto;

import io.micrometer.common.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

@Data
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

	private final UserResponseDto userDto;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
//		String r = userDto.getUserRole();              // admin / USER / ROLE_ADMIN 등
//	    String normalized = r.toUpperCase();
//	    if (!normalized.startsWith("ROLE_")) {
//	        normalized = "ROLE_" + normalized;
//	    }
//	    return List.of(new SimpleGrantedAuthority(normalized));
		List<GrantedAuthority> roles = new ArrayList<>();
        if (userDto.getUserRole() != null) {
            roles.add(new SimpleGrantedAuthority("ROLE_" + userDto.getUserRole()));
        }
        return roles;
	}

	@Override
    public boolean isEnabled() {
        // 이메일 인증이 완료된 유저만 로그인 허용
		System.out.println("userEmail: " + userDto.getUserEmail());
		System.out.println("userEmailVerified: " + userDto.getUserEmailVerified());
		return Boolean.TRUE.equals(userDto.getUserEmailVerified())
		        && "active".equalsIgnoreCase(userDto.getUserStatus());
    }

	@Override
	public String getUsername() {
		return userDto.getUserName();
	}
	
	@Override
	public String getPassword() {
		return userDto.getUserPw();
	}
	
	public String getEmail() {
		return userDto.getUserEmail();
	}
	
	public int getId() {
		return userDto.getUserId();
	}


}
