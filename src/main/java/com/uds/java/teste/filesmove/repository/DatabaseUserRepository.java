package com.uds.java.teste.filesmove.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.uds.java.teste.filesmove.model.Role;
import com.uds.java.teste.filesmove.model.UserAccount;

@Repository
public class DatabaseUserRepository implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	public DatabaseUserRepository(UserJpaRepository userJpaRepository) {
		this.userJpaRepository = userJpaRepository;
	}

	@Override
	public Optional<UserAccount> findByUsername(String username) {
		return this.userJpaRepository.findByUsernameAndEnabledTrue(username)
			.map(user -> new UserAccount(
				user.getUsername(),
				user.getPassword(),
				Role.valueOf(user.getRole())
			));
	}
}
