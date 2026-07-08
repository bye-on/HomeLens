package com.homelens.model.service.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.homelens.mapper.UserMapper;
import com.homelens.model.CustomUserDetails;
import com.homelens.model.response.user.UserResponseDto;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService, CustomUserDetailsService{

	private final UserMapper userMapper;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		System.out.println("loadUserByUsername : " + email);
		UserResponseDto findUserInfoDto = userMapper.findByEmail(email);
		System.out.println(findUserInfoDto);
		
		if (findUserInfoDto == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        return new CustomUserDetails(findUserInfoDto);
	}

}
