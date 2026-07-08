package com.homelens.model.service.user;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.homelens.exception.CustomException;
import com.homelens.exception.ErrorCode;
import com.homelens.mapper.EmailVerificationTokenMapper;
import com.homelens.mapper.UserMapper;
import com.homelens.model.EmailVerificationTokenDto;
import com.homelens.model.request.PageRequestDto;
import com.homelens.model.request.user.JoinRequestDto;
import com.homelens.model.request.user.UserDetailUpdateRequestDto;
import com.homelens.model.request.user.UserPasswordResetRequestDto;
import com.homelens.model.request.user.UserPasswordUpdateRequestDto;
import com.homelens.model.response.user.UserDetailResponseDto;
import com.homelens.model.response.user.UserListResponseDto;
import com.homelens.model.response.user.UserResponseDto;
import com.homelens.model.service.MailService;
import com.homelens.util.TokenUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final EmailVerificationTokenMapper tokenMapper;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final MailService mailService;
	
	public static String generatePassword() {
        // 사용할 문자 세트 정의
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialCharacters = "!@#$%^&*()-_=+<>?";
        int length = 8;
        
        // 모든 문자 세트를 합침
        String allCharacters = upperCase + lowerCase + digits + specialCharacters;
        
        // SecureRandom 객체 사용하여 랜덤 생성
        SecureRandom random = new SecureRandom();
        
        StringBuilder password = new StringBuilder(length);
        
        // 비밀번호 길이만큼 랜덤하게 문자 선택
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allCharacters.length());
            password.append(allCharacters.charAt(randomIndex));
        }
        
        return password.toString();
    }

	@Override
	public void joinProcess(JoinRequestDto joinDto) {
		if (userMapper.existsByEmail(joinDto.getUserEmail()) != 0) {
			UserResponseDto existingUser = userMapper.findByEmail(joinDto.getUserEmail());
			if (existingUser != null && !Boolean.TRUE.equals(existingUser.getUserEmailVerified())) {
				sendVerificationMail(existingUser);
			}
			return;
		}

		joinDto.setUserPw(bCryptPasswordEncoder.encode(joinDto.getUserPw()));

		userMapper.signUp(joinDto);

		UserResponseDto userDto = userMapper.findByEmail(joinDto.getUserEmail());
		sendVerificationMail(userDto);
	}

	private void sendVerificationMail(UserResponseDto userDto) {
		tokenMapper.revokeAllByUserId(userDto.getUserId());

		String rawToken = TokenUtil.newRawToken();
		String tokenHash = TokenUtil.sha256Hex(rawToken);

		EmailVerificationTokenDto tokenDto = new EmailVerificationTokenDto();
		tokenDto.setUserId(userDto.getUserId());
		tokenDto.setTokenHash(tokenHash);
		
		tokenMapper.insertToken(tokenDto);
		
		//메일 전송
		mailService.sendVerifyEmailAsync(userDto.getUserEmail(), rawToken);
	}

	@Override
	public void updateUserRefreshToken(UserResponseDto userDto) {
		userMapper.updateUserRefreshToken(userDto);
	}

	@Override
	public int existsByUsername(String username) {
		return userMapper.existsByUsername(username);
	}

	@Override
	public int existsByEmail(String username) {
		return userMapper.existsByEmail(username);
	}
	
	@Override
	public int existsByUserId(int userId) {
		return userMapper.existsByUserId(userId);
	}

	@Override
	public void signUp(JoinRequestDto joinDto) {
		userMapper.signUp(joinDto);
	}

	@Override
	public UserResponseDto findByUsername(String username) {
		return userMapper.findByUsername(username);
	}

	@Override
	public UserResponseDto findByEmail(String email) {
		return userMapper.findByEmail(email);
	}

	@Override
	public UserDetailResponseDto findByUserId(Integer userId) {
		return userMapper.findByUserId(userId);
	}

	@Override
	public UserListResponseDto getAllUsers(PageRequestDto pageRequestDto) {
		int count = userMapper.countUser();
    	int totalPage = (count / pageRequestDto.getSize()) + 1;
    	int currentPage = pageRequestDto.getPage();
    	
    	if(currentPage > totalPage)
    		currentPage = totalPage;
		
    	UserListResponseDto userListResponseDto = new UserListResponseDto();
    	userListResponseDto.setUserList(userMapper.getAllUsers(pageRequestDto));
    	userListResponseDto.setCurrentPage(currentPage);
    	userListResponseDto.setTotalPage(totalPage);
    	
		return userListResponseDto;
	}

	@Override
	public void softDeleteUsers(int userId) {
		
		if(existsByUserId(userId) == 0) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND, "해당 ID를 가지는 유저가 없습니다.");
		}
		
		userMapper.softDeleteUsers(userId);
	}

	@Override
	public void logout(int userId) {
		userMapper.logout(userId);
		
	}

	@Override
	public void updateUserInfo(UserDetailUpdateRequestDto userDeatilDetailUpdateRequestDto) {
		userMapper.updateUserInfo(userDeatilDetailUpdateRequestDto);
	}

	@Override
	public void updateUserPassword(UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
		userPasswordUpdateRequestDto.setUserPw(bCryptPasswordEncoder.encode(userPasswordUpdateRequestDto.getUserPw()));
		userMapper.updateUserPassword(userPasswordUpdateRequestDto);
	}

	@Override
	public void resetUserPassword(UserPasswordResetRequestDto userPasswordResetRequestDto) {
		UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
		String tempPw = generatePassword();
		userPasswordUpdateRequestDto.setUserId(userMapper.findByEmail(userPasswordResetRequestDto.getUserEmail()).getUserId());
		userPasswordUpdateRequestDto.setUserPw(bCryptPasswordEncoder.encode(tempPw));
		userMapper.updateUserPassword(userPasswordUpdateRequestDto);
		
		mailService.sendResetPassword(userPasswordResetRequestDto.getUserEmail(), tempPw);
	}

	@Override
	public int countAll() {
		return userMapper.countUser();
	}

}
