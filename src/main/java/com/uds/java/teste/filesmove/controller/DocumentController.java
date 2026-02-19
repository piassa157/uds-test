package com.uds.java.teste.filesmove.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	@GetMapping("/ping")
	public ResponseEntity<Map<String, String>> ping(Authentication authentication) {
		return ResponseEntity.ok(
			Map.of(
				"message", "Documents endpoint authorized",
				"user", authentication.getName()
			)
		);
	}
}
