package com.uds.java.teste.filesmove.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.uds.java.teste.filesmove.dto.CreateDocumentRequest;
import com.uds.java.teste.filesmove.dto.DocumentFileVersionResponse;
import com.uds.java.teste.filesmove.dto.DocumentResponse;
import com.uds.java.teste.filesmove.dto.FileUploadResponse;
import com.uds.java.teste.filesmove.dto.UpdateDocumentRequest;
import com.uds.java.teste.filesmove.model.DocumentEntity;
import com.uds.java.teste.filesmove.model.DocumentFileVersionEntity;
import com.uds.java.teste.filesmove.model.DocumentStatus;
import com.uds.java.teste.filesmove.repository.DocumentFileVersionJpaRepository;
import com.uds.java.teste.filesmove.repository.DocumentJpaRepository;

@Service
public class DocumentService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"application/pdf",
		"image/png",
		"image/jpeg",
		"image/jpg"
	);

	private final DocumentJpaRepository documentJpaRepository;
	private final DocumentFileVersionJpaRepository documentFileVersionJpaRepository;
	private final FileStorageService fileStorageService;

	public DocumentService(
		DocumentJpaRepository documentJpaRepository,
		DocumentFileVersionJpaRepository documentFileVersionJpaRepository,
		FileStorageService fileStorageService
	) {
		this.documentJpaRepository = documentJpaRepository;
		this.documentFileVersionJpaRepository = documentFileVersionJpaRepository;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public DocumentResponse create(CreateDocumentRequest request) {
		DocumentEntity document = new DocumentEntity();
		document.setTitle(request.title().trim());
		document.setDescription(request.description());
		document.setTags(normalizeTags(request.tags()));
		document.setOwnerTenant(request.ownerTenant().trim());
		document.setStatus(request.status() == null ? DocumentStatus.DRAFT : request.status());
		return toResponse(this.documentJpaRepository.save(document));
	}

	@Transactional
	public DocumentResponse updateMetadata(Long documentId, UpdateDocumentRequest request) {
		DocumentEntity document = getDocumentOrThrow(documentId);
		document.setTitle(request.title().trim());
		document.setDescription(request.description());
		document.setTags(normalizeTags(request.tags()));
		document.setOwnerTenant(request.ownerTenant().trim());
		return toResponse(this.documentJpaRepository.save(document));
	}

	@Transactional
	public DocumentResponse changeStatus(Long documentId, DocumentStatus status) {
		DocumentEntity document = getDocumentOrThrow(documentId);
		document.setStatus(status);
		return toResponse(this.documentJpaRepository.save(document));
	}

	@Transactional(readOnly = true)
	public DocumentResponse getById(Long documentId) {
		return toResponse(getDocumentOrThrow(documentId));
	}

	@Transactional(readOnly = true)
	public Page<DocumentResponse> list(String title, DocumentStatus status, Pageable pageable) {
		Specification<DocumentEntity> spec = (root, query, cb) -> cb.conjunction();

		if (title != null && !title.isBlank()) {
			String normalizedTitle = "%" + title.toLowerCase(Locale.ROOT) + "%";
			spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), normalizedTitle));
		}

		if (status != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
		}

		return this.documentJpaRepository.findAll(spec, pageable).map(this::toResponse);
	}

	@Transactional
	public FileUploadResponse upload(Long documentId, MultipartFile file, String uploadedBy) {
		DocumentEntity document = getDocumentOrThrow(documentId);
		validateUpload(file);

		int nextVersion = this.documentFileVersionJpaRepository
			.findTopByDocumentIdOrderByVersionNumberDesc(documentId)
			.map(version -> version.getVersionNumber() + 1)
			.orElse(1);

		String fileKey = this.fileStorageService.store(documentId, nextVersion, file);

		DocumentFileVersionEntity version = new DocumentFileVersionEntity();
		version.setDocument(document);
		version.setVersionNumber(nextVersion);
		version.setFileKey(fileKey);
		version.setOriginalFilename(
			file.getOriginalFilename() == null ? "uploaded-file" : file.getOriginalFilename()
		);
		version.setContentType(file.getContentType());
		version.setSizeBytes(file.getSize());
		version.setUploadedBy(uploadedBy);

		DocumentFileVersionEntity saved = this.documentFileVersionJpaRepository.save(version);
		return new FileUploadResponse(
			documentId,
			saved.getVersionNumber(),
			saved.getFileKey(),
			saved.getUploadedAt(),
			saved.getUploadedBy()
		);
	}

	@Transactional(readOnly = true)
	public Resource loadCurrentFile(Long documentId) {
		DocumentFileVersionEntity currentVersion = this.documentFileVersionJpaRepository
			.findTopByDocumentIdOrderByVersionNumberDesc(documentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No file found for document"));
		return this.fileStorageService.loadAsResource(currentVersion.getFileKey());
	}

	@Transactional(readOnly = true)
	public DocumentFileVersionEntity getCurrentVersion(Long documentId) {
		return this.documentFileVersionJpaRepository.findTopByDocumentIdOrderByVersionNumberDesc(documentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No file found for document"));
	}

	@Transactional(readOnly = true)
	public List<DocumentFileVersionResponse> listVersions(Long documentId) {
		getDocumentOrThrow(documentId);
		return this.documentFileVersionJpaRepository.findByDocumentIdOrderByVersionNumberDesc(documentId)
			.stream()
			.map(this::toVersionResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public DocumentFileVersionEntity getVersionByNumber(Long documentId, Integer versionNumber) {
		return this.documentFileVersionJpaRepository.findByDocumentIdAndVersionNumber(documentId, versionNumber)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File version not found"));
	}

	@Transactional(readOnly = true)
	public Resource loadFileVersion(Long documentId, Integer versionNumber) {
		DocumentFileVersionEntity version = getVersionByNumber(documentId, versionNumber);
		return this.fileStorageService.loadAsResource(version.getFileKey());
	}

	private void validateUpload(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"Only PDF, PNG and JPG files are allowed"
			);
		}
	}

	private DocumentEntity getDocumentOrThrow(Long documentId) {
		return this.documentJpaRepository.findById(documentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
	}

	private Set<String> normalizeTags(java.util.List<String> tags) {
		if (tags == null) {
			return new HashSet<>();
		}

		Set<String> normalized = new HashSet<>();
		for (String tag : tags) {
			if (tag == null) {
				continue;
			}
			String trimmed = tag.trim();
			if (!trimmed.isEmpty()) {
				normalized.add(trimmed);
			}
		}
		return normalized;
	}

	private DocumentResponse toResponse(DocumentEntity entity) {
		Set<String> tagsCopy = entity.getTags() == null ? Set.of() : Set.copyOf(entity.getTags());
		return new DocumentResponse(
			entity.getId(),
			entity.getTitle(),
			entity.getDescription(),
			tagsCopy,
			entity.getOwnerTenant(),
			entity.getStatus(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}

	private DocumentFileVersionResponse toVersionResponse(DocumentFileVersionEntity entity) {
		return new DocumentFileVersionResponse(
			entity.getId(),
			entity.getVersionNumber(),
			entity.getFileKey(),
			entity.getOriginalFilename(),
			entity.getContentType(),
			entity.getSizeBytes(),
			entity.getUploadedAt(),
			entity.getUploadedBy()
		);
	}
}
