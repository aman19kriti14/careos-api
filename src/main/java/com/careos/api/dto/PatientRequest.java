package com.careos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PatientRequest {

	@NotBlank(message = "Name is required")
	private String name;

	@NotBlank(message = "Phone is required")
	private String phone;

	@NotNull(message = "Age is required")
	private Integer age;

	private String gender;

	private List<String> conditions;

	private List<String> medications;
}