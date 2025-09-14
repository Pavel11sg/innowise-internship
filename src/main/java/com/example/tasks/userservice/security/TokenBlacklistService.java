package com.example.tasks.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class TokenBlacklistService {
	private final RedisTemplate<String, Object> redisTemplate;

	@Value("${jwt.blacklist.prefix}")
	private String blacklistPrefix;
	private final SecretKey signingKey;

	public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate, @Value("${jwt.secret}") String secret) {
		this.redisTemplate = redisTemplate;
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public boolean isRevoked(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		String jti = claims.getId();
		return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistPrefix + jti));
	}

	private SecretKey getSigningKey() {
		return signingKey;
	}
}
