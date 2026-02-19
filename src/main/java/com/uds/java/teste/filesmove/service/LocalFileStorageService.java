package com.uds.java.teste.filesmove.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LocalFileStorageService implements FileStorageService {

	private final Path rootPath;

	public LocalFileStorageService(
		@Value("${app.storage.local-path:/tmp/filesmove-storage}") String storagePath
	) {
		this.rootPath = Paths.get(storagePath).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.rootPath);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not initialize local storage", ex);
		}
	}

	@Override
	public String store(Long documentId, int versionNumber, MultipartFile file) {
		String originalFilename = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
		String extension = extractExtension(originalFilename);
		String fileName = String.format(
			"doc-%d-v%d-%s%s",
			documentId,
			versionNumber,
			UUID.randomUUID(),
			extension
		);
		Path documentDir = this.rootPath.resolve("documents").resolve(String.valueOf(documentId));
		Path destination = documentDir.resolve(fileName).normalize();

		try {
			Files.createDirectories(documentDir);
			Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
		}

		return this.rootPath.relativize(destination).toString();
	}

	@Override
	public Resource loadAsResource(String fileKey) {
		try {
			Path filePath = this.rootPath.resolve(fileKey).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
			}
			return resource;
		}
		catch (IOException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
		}
	}

	private String extractExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		if (lastDot < 0) {
			return "";
		}
		return filename.substring(lastDot);
	}
}
