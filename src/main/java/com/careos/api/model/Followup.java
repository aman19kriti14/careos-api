package com.careos.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "followups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Followup {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	// AI generated draft message
	@Column(name = "ai_message", columnDefinition = "TEXT")
	private String aiMessage;

	// doctor edited final version
	@Column(name = "final_message", columnDefinition = "TEXT")
	private String finalMessage;

	// draft | approved | sent | skipped
	private String status = "draft";

	// overdue | noshow_risk | post_visit | manual
	@Column(name = "trigger_reason")
	private String triggerReason;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}