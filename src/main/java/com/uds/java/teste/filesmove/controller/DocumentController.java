package com.uds.java.teste.filesmove.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uds.java.teste.filesmove.dto.CreateDocumentRequest;
import com.uds.java.teste.filesmove.dto.DocumentResponse;
import com.uds.java.teste.filesmove.dto.FileUploadResponse;
import com.uds.java.teste.filesmove.dto.UpdateDocumentRequest;
import com.uds.java.teste.filesmove.model.DocumentStatus;
import com.uds.java.teste.filesmove.model.DocumentFileVersionEntity;
import com.uds.java.teste.filesmove.service.DocumentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@PostMapping
	public ResponseEntity<DocumentResponse> create(@Valid @RequestBody CreateDocumentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.documentService.create(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<DocumentResponse> updateMetadata(
		@PathVariable Long id,
		@Valid @RequestBody UpdateDocumentRequest request
	) {
		return ResponseEntity.ok(this.documentService.updateMetadata(id, request));
	}

	@PatchMapping("/{id}/publish")
	public ResponseEntity<DocumentResponse> publish(@PathVariable Long id) {
		return ResponseEntity.ok(this.documentService.changeStatus(id, DocumentStatus.PUBLISHED));
	}

	@PatchMapping("/{id}/archive")
	public ResponseEntity<DocumentResponse> archive(@PathVariable Long id) {
		return ResponseEntity.ok(this.documentService.changeStatus(id, DocumentStatus.ARCHIVED));
	}

	@GetMapping("/{id}")
	public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(this.documentService.getById(id));
	}

	@GetMapping
	public ResponseEntity<Page<DocumentResponse>> list(
		@RequestParam(required = false) String title,
		@RequestParam(required = false) DocumentStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt,desc") String sort
	) {
		String[] sortParts = sort.split(",");
		String sortField = sortParts[0];
		Sort.Direction sortDirection = sortParts.length > 1
			? Sort.Direction.fromString(sortParts[1])
			: Sort.Direction.DESC;
		Sort sorting = Sort.by(sortDirection, sortField);
		Pageable pageable = PageRequest.of(page, size, sorting);
		return ResponseEntity.ok(this.documentService.list(title, status, pageable));
	}

	@PostMapping(value = "/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FileUploadResponse> upload(
		@PathVariable Long id,
		@RequestParam("file") MultipartFile file,
		Authentication authentication
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(this.documentService.upload(id, file, authentication.getName()));
	}

	@GetMapping("/{id}/files/current")
	public ResponseEntity<Resource> downloadCurrent(@PathVariable Long id) {
		Resource resource = this.documentService.loadCurrentFile(id);
		DocumentFileVersionEntity currentVersion = this.documentService.getCurrentVersion(id);

		ContentDisposition disposition = ContentDisposition.attachment()
			.filename(currentVersion.getOriginalFilename(), StandardCharsets.UTF_8)
			.build();

		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(currentVersion.getContentType()))
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
			.body(resource);
	}
}
