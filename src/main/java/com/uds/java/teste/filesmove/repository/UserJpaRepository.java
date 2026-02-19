package com.uds.java.teste.filesmove.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uds.java.teste.filesmove.model.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByUsernameAndEnabledTrue(String username);
}
