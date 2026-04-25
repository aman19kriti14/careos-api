package com.careos.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careos.api.model.Doctor;
import com.careos.api.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DoctorController {

	private final DoctorRepository doctorRepository;

	// register a new doctor
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody Doctor doctor) {
		try {
			if (doctorRepository.existsByPhone(doctor.getPhone())) {
				return ResponseEntity.badRequest().body("Phone already registered");
			}
			Doctor saved = doctorRepository.save(doctor);
			log.info("New doctor registered: {}", saved.getName());
			return ResponseEntity.ok(saved);
		} catch (Exception e) {
			log.error("Registration failed: {}", e.getMessage());
			return ResponseEntity.internalServerError().body("Registration failed");
		}
	}

	// get doctor profile
	@GetMapping("/{doctorId}")
	public ResponseEntity<?> getDoctor(@PathVariable UUID doctorId) {
		return doctorRepository.findById(doctorId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	// update doctor profile
	@PutMapping("/{doctorId}")
	public ResponseEntity<?> updateDoctor(@PathVariable UUID doctorId, @RequestBody Doctor updated) {
		return doctorRepository.findById(doctorId).map(doctor -> {
			doctor.setName(updated.getName());
			doctor.setClinicName(updated.getClinicName());
			doctor.setCity(updated.getCity());
			doctor.setSpeciality(updated.getSpeciality());
			return ResponseEntity.ok(doctorRepository.save(doctor));
		}).orElse(ResponseEntity.notFound().build());
	}
}