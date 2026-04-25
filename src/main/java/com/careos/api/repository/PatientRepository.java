package com.careos.api.repository;

import com.careos.api.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

	List<Patient> findByDoctorId(UUID doctorId);

	List<Patient> findByDoctorIdOrderByCreatedAtDesc(UUID doctorId);

	// find high no-show risk patients for a doctor
	List<Patient> findByDoctorIdAndNoshowScoreGreaterThanEqual(UUID doctorId, Integer score);

	// search patients by name or phone
	@Query("SELECT p FROM Patient p WHERE p.doctor.id = :doctorId "
			+ "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " + "OR p.phone LIKE CONCAT('%', :query, '%'))")
	List<Patient> searchPatients(UUID doctorId, String query);

	boolean existsByDoctorIdAndPhone(UUID doctorId, String phone);
}