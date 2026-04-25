package com.careos.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.careos.api.dto.AppointmentRequest;
import com.careos.api.dto.AppointmentResponse;
import com.careos.api.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/doctors/{doctorId}/appointments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AppointmentController {

	private final AppointmentService appointmentService;

	// book new appointment
	@PostMapping
	public ResponseEntity<?> bookAppointment(@PathVariable UUID doctorId,
			@Valid @RequestBody AppointmentRequest request) {
		try {
			AppointmentResponse response = appointmentService.bookAppointment(doctorId, request);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// get today's appointments
	@GetMapping("/today")
	public ResponseEntity<List<AppointmentResponse>> getTodayAppointments(@PathVariable UUID doctorId) {
		return ResponseEntity.ok(appointmentService.getTodayAppointments(doctorId));
	}

	// get available slots for a date — used by patient booking page
	@GetMapping("/slots")
	public ResponseEntity<List<AppointmentResponse>> getAvailableSlots(@PathVariable UUID doctorId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
	}

	// update appointment status
	// status: waiting | done | no_show | cancelled
	@PatchMapping("/{appointmentId}/status")
	public ResponseEntity<?> updateStatus(@PathVariable UUID doctorId, @PathVariable UUID appointmentId,
			@RequestParam String status) {
		try {
			return ResponseEntity.ok(appointmentService.updateStatus(doctorId, appointmentId, status));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// save visit notes
	@PatchMapping("/{appointmentId}/notes")
	public ResponseEntity<?> updateNotes(@PathVariable UUID doctorId, @PathVariable UUID appointmentId,
			@RequestParam String notes) {
		try {
			return ResponseEntity.ok(appointmentService.updateNotes(doctorId, appointmentId, notes));
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}