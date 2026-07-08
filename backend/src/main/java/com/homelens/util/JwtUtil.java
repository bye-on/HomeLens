package com.homelens.util;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.homelens.model.response.user.UserResponseDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final SecretKey secretKey;

	public JwtUtil() {
		secretKey = Jwts.SIG.HS256.key().build();
	}

	@Value("${ssafy.jwt.access-expmin}")
	private long accessExpMin;

	@Value("${ssafy.jwt.refresh-expmin}")
	private long refreshExpMin;

	public String getUserId(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId",
				String.class);
	}

	public String getEmail(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userEmail",
				String.class);
	}

	public String getUsername(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userName",
				String.class);
	}

	public String getRole(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userRole",
				String.class);
	}

	public Boolean isExpired(String token) {

		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration()
				.before(new Date());
	}

	public String createJwt(String subject, long expireMin, Map<String, Object> claims) {
		Date expireDate = new Date(System.currentTimeMillis() + 1000 * 60 * expireMin);
		String jwt = Jwts.builder().subject(subject).expiration(expireDate).claims(claims).signWith(secretKey)
				.compact();

		return jwt;
	}

	public String createAccessToken(UserResponseDto userDto) {
		return "Bearer "+ createJwt("accessToken", accessExpMin, Map.of("userId", userDto.getUserId(), "userEmail",
				userDto.getUserEmail(), "userName", userDto.getUserName(), "userRole", userDto.getUserRole()));
	}

	public String createRefreshToken(UserResponseDto userDto) {
		return createJwt("refreshToken", refreshExpMin, Map.of("userEmail", userDto.getUserEmail()));
	}

	public Claims getClaims(String jwt) {
		// TODO: 03-2. JWT토큰을 검증하고 claim 정보를 반환하는 코드를 살펴보세요.

		JwtParser parser = Jwts.parser().verifyWith(secretKey).build();

		var jws = parser.parseSignedClaims(jwt);

		return jws.getPayload();

		// END
	}
}
