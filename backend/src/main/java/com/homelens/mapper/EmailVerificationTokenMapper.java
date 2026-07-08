package com.homelens.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.homelens.model.EmailVerificationTokenDto;

@Mapper
public interface EmailVerificationTokenMapper {
    int insertToken(EmailVerificationTokenDto token);

    // 동시 클릭 대비: FOR UPDATE로 잠그고 가져오기
    EmailVerificationTokenDto selectValidTokenForUpdate(String tokenHash);

    int markUsed(int id);
    int revokeAllByUserId(int userId); // (선택) 재발급 시 기존 토큰 무효화
}

