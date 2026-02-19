package com.uds.java.teste.filesmove.dto;

import java.time.Instant;
import java.util.Set;

import com.uds.java.teste.filesmove.model.DocumentStatus;

public record DocumentResponse(
	Long id,
	String title,
	String description,
	Set<String> tags,
	String ownerTenant,
	DocumentStatus status,
	Instant createdAt,
	Instant updatedAt
) {
}
