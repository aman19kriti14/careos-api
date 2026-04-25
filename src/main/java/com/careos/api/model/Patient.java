package com.careos.api.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String phone;

	private Integer age;

	private String gender;

	@ElementCollection
	@CollectionTable(name = "patient_conditions", joinColumns = @JoinColumn(name = "patient_id"))
	@Column(name = "condition")
	private List<String> conditions;

	@ElementCollection
	@CollectionTable(name = "patient_medications", joinColumns = @JoinColumn(name = "patient_id"))
	@Column(name = "medication")
	private List<String> medications;

	// 0-100, computed by AI — higher means more likely to no-show
	@Column(name = "noshow_score")
	private Integer noshowScore = 0;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}