package com.uds.java.teste.filesmove.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

	private static final String SECRET = "filesmove-super-secret-key-32-characters-min";

	@Test
	void shouldGenerateAndValidateTokenForUser() {
		JwtService jwtService = new JwtService(SECRET, 60_000L);
		UserDetails user = User.withUsername("user")
			.password("ignored")
			.roles("USER")
			.build();

		String token = jwtService.generateToken(user);

		assertEquals("user", jwtService.extractUsername(token));
		assertTrue(jwtService.isTokenValid(token, user));
	}

	@Test
	void shouldInvalidateTokenForDifferentUser() {
		JwtService jwtService = new JwtService(SECRET, 60_000L);
		UserDetails user = User.withUsername("user")
			.password("ignored")
			.roles("USER")
			.build();
		UserDetails anotherUser = User.withUsername("admin")
			.password("ignored")
			.roles("ADMIN")
			.build();

		String token = jwtService.generateToken(user);

		assertFalse(jwtService.isTokenValid(token, anotherUser));
	}
}
