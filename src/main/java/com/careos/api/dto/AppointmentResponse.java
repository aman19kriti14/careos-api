package com.careos.api.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AppointmentResponse {

	private UUID id;
	private String patientName;
	private String patientPhone;
	private String reason;
	private LocalDateTime scheduledAt;
	private String status;
	private Boolean smsSent;
	private String notes;
	private LocalDateTime createdAt;

	// patient details
	private Integer patientAge;
	private String patientGender;
	private Long totalVisits;
	private Integer noshowScore;
}