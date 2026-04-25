package com.careos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequest {

	@NotNull(message = "Patient ID is required")
	private UUID patientId;

	@NotNull(message = "Scheduled time is required")
	private LocalDateTime scheduledAt;

	@NotBlank(message = "Reason is required")
	private String reason;
}