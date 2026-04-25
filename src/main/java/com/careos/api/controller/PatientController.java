package com.careos.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.careos.api.dto.PatientRequest;
import com.careos.api.dto.PatientResponse;
import com.careos.api.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/doctors/{doctorId}/patients")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PatientController {

	private final PatientService patientService;

	// add new patient
	@PostMapping
	public ResponseEntity<?> addPatient(@PathVariable UUID doctorId, @Valid @RequestBody PatientRequest request) {
		try {
			PatientResponse response = patientService.addPatient(doctorId, request);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// get all patients
	@GetMapping
	public ResponseEntity<List<PatientResponse>> getAllPatients(@PathVariable UUID doctorId) {
		return ResponseEntity.ok(patientService.getAllPatients(doctorId));
	}

	// get single patient
	@GetMapping("/{patientId}")
	public ResponseEntity<?> getPatient(@PathVariable UUID doctorId, @PathVariable UUID patientId) {
		try {
			return ResponseEntity.ok(patientService.getPatient(doctorId, patientId));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// search patients
	@GetMapping("/search")
	public ResponseEntity<List<PatientResponse>> searchPatients(@PathVariable UUID doctorId,
			@RequestParam String query) {
		return ResponseEntity.ok(patientService.searchPatients(doctorId, query));
	}

	// get high risk patients
	@GetMapping("/high-risk")
	public ResponseEntity<List<PatientResponse>> getHighRiskPatients(@PathVariable UUID doctorId) {
		return ResponseEntity.ok(patientService.getHighRiskPatients(doctorId));
	}

	// update patient
	@PutMapping("/{patientId}")
	public ResponseEntity<?> updatePatient(@PathVariable UUID doctorId, @PathVariable UUID patientId,
			@Valid @RequestBody PatientRequest request) {
		try {
			return ResponseEntity.ok(patientService.updatePatient(doctorId, patientId, request));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}