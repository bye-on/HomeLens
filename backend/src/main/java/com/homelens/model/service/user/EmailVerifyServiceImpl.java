package com.homelens.model.service.user;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homelens.mapper.EmailVerificationTokenMapper;
import com.homelens.mapper.UserMapper;
import com.homelens.model.EmailVerificationTokenDto;
import com.homelens.util.TokenUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerifyServiceImpl implements EmailVerifyService{
	private final EmailVerificationTokenMapper tokenMapper;
    private final UserMapper userMapper;

    @Transactional
    public void verify(String rawToken) {
        String tokenHash = TokenUtil.sha256Hex(rawToken);

        EmailVerificationTokenDto t = tokenMapper.selectValidTokenForUpdate(tokenHash);
        if (t == null) throw new IllegalArgumentException("INVALID_TOKEN");

        if (t.getExpiresAt().isBefore(OffsetDateTime.now())) {
            // 만료면 필요시 revoked 처리 후 예외
            throw new IllegalArgumentException("EXPIRED_TOKEN");
        }

        int used = tokenMapper.markUsed(t.getId());
        if (used != 1) throw new IllegalStateException("TOKEN_ALREADY_USED");

        userMapper.setEmailVerified(t.getUserId());
    }
}
