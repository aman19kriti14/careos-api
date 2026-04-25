package com.careos.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.careos.api.model.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

	// all appointments for a doctor today
	@Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId "
			+ "AND a.scheduledAt >= :start AND a.scheduledAt < :end " + "ORDER BY a.scheduledAt ASC")
	List<Appointment> findTodayAppointments(UUID doctorId, LocalDateTime start, LocalDateTime end);

	// all upcoming appointments for a patient
	List<Appointment> findByPatientIdAndStatusOrderByScheduledAtAsc(UUID patientId, String status);

	// appointments by doctor and status
	List<Appointment> findByDoctorIdAndStatusOrderByScheduledAtAsc(UUID doctorId, String status);

	// available slots — find booked times for a doctor on a date
	@Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId "
			+ "AND a.scheduledAt >= :start AND a.scheduledAt < :end " + "AND a.status != 'cancelled'")
	List<Appointment> findBookedSlots(UUID doctorId, LocalDateTime start, LocalDateTime end);

	// SMS reminders not yet sent for upcoming appointments
	@Query("SELECT a FROM Appointment a WHERE a.smsSent = false " + "AND a.status = 'upcoming' "
			+ "AND a.scheduledAt BETWEEN :from AND :to")
	List<Appointment> findAppointmentsNeedingReminder(LocalDateTime from, LocalDateTime to);

	// count appointments by patient
	long countByPatientId(UUID patientId);

	@Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " + "AND a.doctor.id = :doctorId "
			+ "ORDER BY a.scheduledAt DESC")
	List<Appointment> findByPatientAndDoctor(UUID patientId, UUID doctorId);
}