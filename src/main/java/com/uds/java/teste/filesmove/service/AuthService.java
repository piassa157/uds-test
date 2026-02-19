package com.uds.java.teste.filesmove.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.uds.java.teste.filesmove.dto.AuthRequest;
import com.uds.java.teste.filesmove.dto.AuthResponse;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	public AuthResponse authenticate(AuthRequest request) {
		Authentication authentication = this.authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.username(), request.password())
		);
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String token = this.jwtService.generateToken(userDetails);

		return new AuthResponse(token, "Bearer", this.jwtService.getExpirationMs() / 1000);
	}
}
