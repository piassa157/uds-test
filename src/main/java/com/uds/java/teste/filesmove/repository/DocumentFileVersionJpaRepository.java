package com.uds.java.teste.filesmove.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uds.java.teste.filesmove.model.DocumentFileVersionEntity;

public interface DocumentFileVersionJpaRepository extends JpaRepository<DocumentFileVersionEntity, Long> {

	Optional<DocumentFileVersionEntity> findTopByDocumentIdOrderByVersionNumberDesc(Long documentId);

	List<DocumentFileVersionEntity> findByDocumentIdOrderByVersionNumberDesc(Long documentId);

	Optional<DocumentFileVersionEntity> findByDocumentIdAndVersionNumber(Long documentId, Integer versionNumber);
}
