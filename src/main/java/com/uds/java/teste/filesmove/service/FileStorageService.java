package com.uds.java.teste.filesmove.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

	String store(Long documentId, int versionNumber, MultipartFile file);

	Resource loadAsResource(String fileKey);
}
