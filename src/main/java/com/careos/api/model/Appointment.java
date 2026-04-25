package com.careos.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@Column(name = "scheduled_at", nullable = false)
	private LocalDateTime scheduledAt;

	private String reason;

	// upcoming | waiting | done | no_show | cancelled
	private String status = "upcoming";

	// doctor's visit notes — AI will structure these
	private String notes;

	@Column(name = "sms_sent")
	private Boolean smsSent = false;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}