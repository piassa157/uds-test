package com.uds.java.teste.filesmove.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.uds.java.teste.filesmove.model.Role;
import com.uds.java.teste.filesmove.model.UserAccount;

@Repository
public class InMemoryUserRepository implements UserRepository {

	private final Map<String, UserAccount> users;

	public InMemoryUserRepository(
		PasswordEncoder passwordEncoder,
		@Value("${spring.security.user.name:admin}") String adminUsername,
		@Value("${spring.security.user.password:admin}") String adminPassword
	) {
		this.users = Map.of(
			adminUsername, new UserAccount(adminUsername, passwordEncoder.encode(adminPassword), Role.ADMIN),
			"user", new UserAccount("user", passwordEncoder.encode("user123"), Role.USER)
		);
	}

	@Override
	public Optional<UserAccount> findByUsername(String username) {
		return Optional.ofNullable(this.users.get(username));
	}
}
