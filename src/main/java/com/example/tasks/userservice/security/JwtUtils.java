package com.example.tasks.userservice.security;

import com.example.tasks.userservice.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {
	private final TokenBlacklistService blacklistService;

	private final SecretKey signingKey;

	public JwtUtils(TokenBlacklistService blacklistService, @Value("${jwt.secret}") String secret) {
		this.blacklistService = blacklistService;
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public boolean validateAccessToken(String token) {
		if (token == null || token.isBlank()) {
			throw new InvalidTokenException("Token cannot be empty");
		}
		if (isTokenExpired(token)) {
			throw new InvalidTokenException("Token expired");
		}
		if (blacklistService.isRevoked(token)) {
			throw new InvalidTokenException("Token revoked");
		}
		try {
			Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.error("ExpiredJwtException caught, throwing InvalidTokenException");
			throw new InvalidTokenException("Token expired");
		} catch (JwtException | IllegalArgumentException e) {
			log.error("JwtException caught, throwing InvalidTokenException");
			throw new InvalidTokenException("Invalid token");
		}
	}


	private boolean isTokenExpired(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return claims.getExpiration().toInstant().isBefore(Instant.now());
		} catch (ExpiredJwtException e) {
			return true;
		} catch (JwtException e) {
			log.error("JWT validation failed", e);
			throw new InvalidTokenException("Invalid token");
		}
	}

	public UUID getUserIdFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return UUID.fromString(claims.get("userId", String.class));
	}

	public List<String> getRolesFromToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.get("roles", List.class);
	}

	public SecretKey getSigningKey() {
		return signingKey;
	}

}