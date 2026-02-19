package com.uds.java.teste.filesmove.dto;

public record AuthResponse(
	String accessToken,
	String tokenType,
	long expiresIn
) {
}
