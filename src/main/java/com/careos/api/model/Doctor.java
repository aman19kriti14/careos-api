package com.careos.api.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String phone;

	@Column(unique = true)
	private String email;

	@Column(name = "clinic_name")
	private String clinicName;

	private String city;

	private String speciality = "General Physician";

	// trial | starter | pro | growth
	private String plan = "trial";

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}