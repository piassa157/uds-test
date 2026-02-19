package com.uds.java.teste.filesmove.repository;

import java.util.Optional;

import com.uds.java.teste.filesmove.model.UserAccount;

public interface UserRepository {

	Optional<UserAccount> findByUsername(String username);
}
