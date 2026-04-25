package com.careos.api.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FollowupResponse {

	private UUID id;
	private UUID patientId;
	private String patientName;
	private String patientPhone;
	private String aiMessage;
	private String finalMessage;
	private String status;
	private String triggerReason;
	private Integer noshowScore;
	private LocalDateTime sentAt;
	private LocalDateTime createdAt;
}