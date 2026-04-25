package com.careos.api.service;

import com.careos.api.dto.PatientRequest;
import com.careos.api.dto.PatientResponse;
import com.careos.api.model.Doctor;
import com.careos.api.model.Patient;
import com.careos.api.repository.AppointmentRepository;
import com.careos.api.repository.DoctorRepository;
import com.careos.api.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;
	private final AppointmentRepository appointmentRepository;

	public PatientResponse addPatient(UUID doctorId, PatientRequest request) {
		Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));

		if (patientRepository.existsByDoctorIdAndPhone(doctorId, request.getPhone())) {
			throw new RuntimeException("Patient with this phone " + "already exists in your clinic");
		}

		Patient patient = new Patient();
		patient.setDoctor(doctor);
		patient.setName(request.getName());
		patient.setPhone(request.getPhone());
		patient.setAge(request.getAge());
		patient.setGender(request.getGender());
		patient.setConditions(request.getConditions());
		patient.setMedications(request.getMedications());

		Patient saved = patientRepository.save(patient);
		log.info("New patient added: {} for doctor: {}", saved.getName(), doctorId);

		return mapToResponse(saved);
	}

	public List<PatientResponse> getAllPatients(UUID doctorId) {
		return patientRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream().map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public PatientResponse getPatient(UUID doctorId, UUID patientId) {
		Patient patient = patientRepository.findById(patientId)
				.orElseThrow(() -> new RuntimeException("Patient not found"));

		if (!patient.getDoctor().getId().equals(doctorId)) {
			throw new RuntimeException("Access denied");
		}

		return mapToResponse(patient);
	}

	public List<PatientResponse> searchPatients(UUID doctorId, String query) {
		return patientRepository.searchPatients(doctorId, query).stream().map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	public List<PatientResponse> getHighRiskPatients(UUID doctorId) {
		// patients with noshow score >= 60 are high risk
		return patientRepository.findByDoctorIdAndNoshowScoreGreaterThanEqual(doctorId, 60).stream()
				.map(this::mapToResponse).collect(Collectors.toList());
	}

	public PatientResponse updatePatient(UUID doctorId, UUID patientId, PatientRequest request) {
		Patient patient = patientRepository.findById(patientId)
				.orElseThrow(() -> new RuntimeException("Patient not found"));

		if (!patient.getDoctor().getId().equals(doctorId)) {
			throw new RuntimeException("Access denied");
		}

		patient.setName(request.getName());
		patient.setPhone(request.getPhone());
		patient.setAge(request.getAge());
		patient.setGender(request.getGender());
		patient.setConditions(request.getConditions());
		patient.setMedications(request.getMedications());

		return mapToResponse(patientRepository.save(patient));
	}

	private PatientResponse mapToResponse(Patient patient) {
		long totalVisits = appointmentRepository.countByPatientId(patient.getId());

		return PatientResponse.builder().id(patient.getId()).name(patient.getName()).phone(patient.getPhone())
				.age(patient.getAge()).gender(patient.getGender()).conditions(patient.getConditions())
				.medications(patient.getMedications()).noshowScore(patient.getNoshowScore()).totalVisits(totalVisits)
				.createdAt(patient.getCreatedAt()).build();
	}
}