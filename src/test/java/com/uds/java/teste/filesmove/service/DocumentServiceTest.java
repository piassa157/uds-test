package com.uds.java.teste.filesmove.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.uds.java.teste.filesmove.dto.CreateDocumentRequest;
import com.uds.java.teste.filesmove.dto.FileUploadResponse;
import com.uds.java.teste.filesmove.dto.UpdateDocumentRequest;
import com.uds.java.teste.filesmove.model.DocumentEntity;
import com.uds.java.teste.filesmove.model.DocumentFileVersionEntity;
import com.uds.java.teste.filesmove.model.DocumentStatus;
import com.uds.java.teste.filesmove.repository.DocumentFileVersionJpaRepository;
import com.uds.java.teste.filesmove.repository.DocumentJpaRepository;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

	@Mock
	private DocumentJpaRepository documentJpaRepository;

	@Mock
	private DocumentFileVersionJpaRepository documentFileVersionJpaRepository;

	@Mock
	private FileStorageService fileStorageService;

	private DocumentService documentService;

	@BeforeEach
	void setUp() {
		this.documentService = new DocumentService(
			this.documentJpaRepository,
			this.documentFileVersionJpaRepository,
			this.fileStorageService
		);
	}

	@Test
	void createShouldPersistDocumentWithDefaultDraftStatus() {
		CreateDocumentRequest request = new CreateDocumentRequest(
			"Documento 1",
			"Descricao",
			List.of("financeiro", "2026"),
			"tenant-a",
			null
		);
		when(this.documentJpaRepository.save(any(DocumentEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		var response = this.documentService.create(request);

		assertEquals("Documento 1", response.title());
		assertEquals("Descricao", response.description());
		assertEquals("tenant-a", response.ownerTenant());
		assertEquals(DocumentStatus.DRAFT, response.status());
		assertTrue(response.tags().contains("financeiro"));
	}

	@Test
	void updateMetadataShouldUpdateExistingDocument() {
		DocumentEntity entity = documentEntity(1L, "Antigo", DocumentStatus.DRAFT);
		when(this.documentJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
		when(this.documentJpaRepository.save(any(DocumentEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		UpdateDocumentRequest request = new UpdateDocumentRequest(
			"Novo Titulo",
			"Nova descricao",
			List.of("tag-a", "tag-b"),
			"tenant-b"
		);

		var response = this.documentService.updateMetadata(1L, request);

		assertEquals("Novo Titulo", response.title());
		assertEquals("Nova descricao", response.description());
		assertEquals("tenant-b", response.ownerTenant());
		assertEquals(Set.of("tag-a", "tag-b"), response.tags());
	}

	@Test
	void changeStatusShouldPublishDocument() {
		DocumentEntity entity = documentEntity(1L, "Doc", DocumentStatus.DRAFT);
		when(this.documentJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
		when(this.documentJpaRepository.save(any(DocumentEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		var response = this.documentService.changeStatus(1L, DocumentStatus.PUBLISHED);

		assertEquals(DocumentStatus.PUBLISHED, response.status());
	}

	@Test
	void listShouldReturnMappedPage() {
		DocumentEntity entity = documentEntity(1L, "Contrato", DocumentStatus.PUBLISHED);
		Page<DocumentEntity> page = new PageImpl<>(List.of(entity));
		when(this.documentJpaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
			.thenReturn(page);

		var response = this.documentService.list("cont", DocumentStatus.PUBLISHED, PageRequest.of(0, 10));

		assertEquals(1, response.getTotalElements());
		assertEquals("Contrato", response.getContent().get(0).title());
		assertEquals(DocumentStatus.PUBLISHED, response.getContent().get(0).status());
	}

	@Test
	void uploadShouldCreateNewVersion() {
		DocumentEntity entity = documentEntity(10L, "Documento Upload", DocumentStatus.DRAFT);
		when(this.documentJpaRepository.findById(10L)).thenReturn(Optional.of(entity));
		when(this.documentFileVersionJpaRepository.findTopByDocumentIdOrderByVersionNumberDesc(10L))
			.thenReturn(Optional.empty());
		when(this.fileStorageService.store(eq(10L), eq(1), any(MultipartFile.class)))
			.thenReturn("documents/10/file-v1.pdf");
		when(this.documentFileVersionJpaRepository.save(any(DocumentFileVersionEntity.class)))
			.thenAnswer(invocation -> {
				DocumentFileVersionEntity saved = invocation.getArgument(0);
				setField(saved, "uploadedAt", Instant.now());
				return saved;
			});

		MultipartFile file = new org.springframework.mock.web.MockMultipartFile(
			"file",
			"arquivo.pdf",
			"application/pdf",
			"conteudo".getBytes()
		);

		FileUploadResponse response = this.documentService.upload(10L, file, "admin");

		assertEquals(10L, response.documentId());
		assertEquals(1, response.versionNumber());
		assertEquals("documents/10/file-v1.pdf", response.fileKey());
		assertEquals("admin", response.uploadedBy());
		assertNotNull(response.uploadedAt());
	}

	@Test
	void uploadShouldRejectUnsupportedFileType() {
		DocumentEntity entity = documentEntity(11L, "Doc", DocumentStatus.DRAFT);
		when(this.documentJpaRepository.findById(11L)).thenReturn(Optional.of(entity));

		MultipartFile file = new org.springframework.mock.web.MockMultipartFile(
			"file",
			"arquivo.txt",
			"text/plain",
			"conteudo".getBytes()
		);

		ResponseStatusException ex = assertThrows(
			ResponseStatusException.class,
			() -> this.documentService.upload(11L, file, "user")
		);

		assertEquals(400, ex.getStatusCode().value());
	}

	@Test
	void loadCurrentFileShouldReturnResource() {
		DocumentFileVersionEntity version = new DocumentFileVersionEntity();
		setField(version, "fileKey", "documents/12/file-v2.pdf");
		when(this.documentFileVersionJpaRepository.findTopByDocumentIdOrderByVersionNumberDesc(12L))
			.thenReturn(Optional.of(version));

		Resource resource = new ByteArrayResource("ok".getBytes());
		when(this.fileStorageService.loadAsResource("documents/12/file-v2.pdf")).thenReturn(resource);

		Resource returned = this.documentService.loadCurrentFile(12L);

		assertNotNull(returned);
		verify(this.fileStorageService).loadAsResource("documents/12/file-v2.pdf");
	}

	private static DocumentEntity documentEntity(Long id, String title, DocumentStatus status) {
		DocumentEntity entity = new DocumentEntity();
		setField(entity, "id", id);
		entity.setTitle(title);
		entity.setDescription("desc");
		entity.setOwnerTenant("tenant");
		entity.setTags(Set.of("tag1"));
		entity.setStatus(status);
		setField(entity, "createdAt", Instant.now());
		setField(entity, "updatedAt", Instant.now());
		return entity;
	}

	private static void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException("Could not set field " + fieldName, e);
		}
	}
}
