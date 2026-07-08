package com.homelens.model;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class EmailVerificationTokenDto {
	private int id;
    private int userId;
    private String tokenHash;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;
//    private OffsetDateTime revokedAt;
}
