package com.uds.java.teste.filesmove.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final String secret;
	private final long expirationMs;

	public JwtService(
		@Value("${app.security.jwt.secret:filesmove-super-secret-key-32-characters-min}") String secret,
		@Value("${app.security.jwt.expiration-ms:86400000}") long expirationMs
	) {
		this.secret = secret;
		this.expirationMs = expirationMs;
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(
			"roles",
			userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
		);
		return generateToken(claims, userDetails);
	}

	public long getExpirationMs() {
		return this.expirationMs;
	}

	private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		Date now = new Date();
		Date expiresAt = new Date(now.getTime() + this.expirationMs);

		return Jwts.builder()
			.claims(extraClaims)
			.subject(userDetails.getUsername())
			.issuedAt(now)
			.expiration(expiresAt)
			.signWith(getSignInKey())
			.compact();
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSignInKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
