package com.uds.java.teste.filesmove.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.uds.java.teste.filesmove.model.DocumentEntity;

public interface DocumentJpaRepository
	extends JpaRepository<DocumentEntity, Long>, JpaSpecificationExecutor<DocumentEntity> {
}
