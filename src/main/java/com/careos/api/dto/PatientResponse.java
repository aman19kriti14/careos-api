package com.careos.api.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PatientResponse {

	private UUID id;
	private String name;
	private String phone;
	private Integer age;
	private String gender;
	private List<String> conditions;
	private List<String> medications;
	private Integer noshowScore;
	private Long totalVisits;
	private LocalDateTime lastVisit;
	private LocalDateTime createdAt;
}