package com.uds.java.teste.filesmove.dto;

import java.time.Instant;

public record DocumentFileVersionResponse(
	Long id,
	Integer versionNumber,
	String fileKey,
	String originalFilename,
	String contentType,
	Long sizeBytes,
	Instant uploadedAt,
	String uploadedBy
) {
}
