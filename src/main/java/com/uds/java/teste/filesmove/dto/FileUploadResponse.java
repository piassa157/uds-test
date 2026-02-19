package com.uds.java.teste.filesmove.dto;

import java.time.Instant;

public record FileUploadResponse(
	Long documentId,
	Integer versionNumber,
	String fileKey,
	Instant uploadedAt,
	String uploadedBy
) {
}
