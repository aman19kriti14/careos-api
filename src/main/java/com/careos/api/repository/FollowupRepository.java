package com.careos.api.repository;

import com.careos.api.model.Followup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FollowupRepository extends JpaRepository<Followup, UUID> {

	// all draft followups for a doctor
	List<Followup> findByDoctorIdAndStatusOrderByCreatedAtDesc(UUID doctorId, String status);

	// all followups for a specific patient
	List<Followup> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

	// check if a draft already exists for this patient
	boolean existsByPatientIdAndStatus(UUID patientId, String status);

	// all pending followups across all doctors — for scheduler
	@Query("SELECT f FROM Followup f WHERE f.status = 'approved'")
	List<Followup> findAllApproved();

	// count pending drafts for a doctor
	long countByDoctorIdAndStatus(UUID doctorId, String status);
}