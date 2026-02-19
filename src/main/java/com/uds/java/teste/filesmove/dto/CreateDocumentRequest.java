package com.uds.java.teste.filesmove.dto;

import java.util.List;

import com.uds.java.teste.filesmove.model.DocumentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDocumentRequest(
	@NotBlank(message = "title is required")
	@Size(max = 255, message = "title max length is 255")
	String title,

	@Size(max = 4000, message = "description max length is 4000")
	String description,

	List<String> tags,

	@NotBlank(message = "ownerTenant is required")
	@Size(max = 100, message = "ownerTenant max length is 100")
	String ownerTenant,

	DocumentStatus status
) {
}
