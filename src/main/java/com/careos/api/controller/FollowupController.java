package com.careos.api.controller;

import com.careos.api.dto.FollowupResponse;
import com.careos.api.service.FollowupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors/{doctorId}/followups")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FollowupController {

	private final FollowupService followupService;

	// get all draft followups for doctor to review
	@GetMapping
	public ResponseEntity<List<FollowupResponse>> getDraftFollowups(@PathVariable UUID doctorId) {
		return ResponseEntity.ok(followupService.getDraftFollowups(doctorId));
	}

	// doctor approves and sends followup
	// optionally pass edited message in body
	@PostMapping("/{followupId}/send")
	public ResponseEntity<?> sendFollowup(@PathVariable UUID doctorId, @PathVariable UUID followupId,
			@RequestBody(required = false) String finalMessage) {
		try {
			return ResponseEntity.ok(followupService.sendFollowup(doctorId, followupId, finalMessage));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// doctor skips a followup
	@PostMapping("/{followupId}/skip")
	public ResponseEntity<?> skipFollowup(@PathVariable UUID doctorId, @PathVariable UUID followupId) {
		try {
			return ResponseEntity.ok(followupService.skipFollowup(doctorId, followupId));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}