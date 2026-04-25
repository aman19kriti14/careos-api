package com.careos.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.careos.api.dto.AppointmentRequest;
import com.careos.api.dto.AppointmentResponse;
import com.careos.api.model.Appointment;
import com.careos.api.model.Doctor;
import com.careos.api.model.Patient;
import com.careos.api.repository.AppointmentRepository;
import com.careos.api.repository.DoctorRepository;
import com.careos.api.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;
	private final SmsService smsService;

	public AppointmentResponse bookAppointment(UUID doctorId, AppointmentRequest request) {
		Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));

		Patient patient = patientRepository.findById(request.getPatientId())
				.orElseThrow(() -> new RuntimeException("Patient not found"));

		// check slot is not already booked
		List<Appointment> existing = appointmentRepository.findBookedSlots(doctorId,
				request.getScheduledAt().minusMinutes(14), request.getScheduledAt().plusMinutes(14));

		if (!existing.isEmpty()) {
			throw new RuntimeException("This slot is already booked");
		}

		Appointment appointment = new Appointment();
		appointment.setDoctor(doctor);
		appointment.setPatient(patient);
		appointment.setScheduledAt(request.getScheduledAt());
		appointment.setReason(request.getReason());
		appointment.setStatus("upcoming");

		Appointment saved = appointmentRepository.save(appointment);

		// send confirmation SMS immediately
		String dateTime = request.getScheduledAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
		String bookingLink = smsService.buildBookingLink(doctorId.toString());
		String message = smsService.buildReminderMessage(patient.getName(), doctor.getName(), dateTime, bookingLink);
		boolean smsSent = smsService.sendSms(patient.getPhone(), message);

		saved.setSmsSent(smsSent);
		appointmentRepository.save(saved);

		log.info("Appointment booked for patient: {} at {}", patient.getName(), request.getScheduledAt());

		return mapToResponse(saved);
	}

	public List<AppointmentResponse> getTodayAppointments(UUID doctorId) {
		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
		LocalDateTime endOfDay = startOfDay.plusDays(1);

		return appointmentRepository.findTodayAppointments(doctorId, startOfDay, endOfDay).stream()
				.map(this::mapToResponse).collect(Collectors.toList());
	}

	public List<AppointmentResponse> getAvailableSlots(UUID doctorId, LocalDate date) {
		// clinic hours 9am to 6pm, 30 min slots
		LocalDateTime start = date.atTime(9, 0);
		LocalDateTime end = date.atTime(18, 0);

		List<Appointment> booked = appointmentRepository.findBookedSlots(doctorId, start, end);

		List<LocalDateTime> bookedTimes = booked.stream().map(Appointment::getScheduledAt).collect(Collectors.toList());

		// generate all slots and filter out booked ones
		List<LocalDateTime> allSlots = new java.util.ArrayList<>();
		LocalDateTime slot = start;
		while (slot.isBefore(end)) {
			if (!bookedTimes.contains(slot)) {
				allSlots.add(slot);
			}
			slot = slot.plusMinutes(30);
		}

		return allSlots.stream().map(s -> AppointmentResponse.builder().scheduledAt(s).status("available").build())
				.collect(Collectors.toList());
	}

	public AppointmentResponse updateStatus(UUID doctorId, UUID appointmentId, String status) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		if (!appointment.getDoctor().getId().equals(doctorId)) {
			throw new RuntimeException("Access denied");
		}

		appointment.setStatus(status);
		return mapToResponse(appointmentRepository.save(appointment));
	}

	public AppointmentResponse updateNotes(UUID doctorId, UUID appointmentId, String notes) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new RuntimeException("Appointment not found"));

		if (!appointment.getDoctor().getId().equals(doctorId)) {
			throw new RuntimeException("Access denied");
		}

		appointment.setNotes(notes);
		return mapToResponse(appointmentRepository.save(appointment));
	}

	private AppointmentResponse mapToResponse(Appointment a) {
		long totalVisits = appointmentRepository.countByPatientId(a.getPatient().getId());

		return AppointmentResponse.builder().id(a.getId()).patientName(a.getPatient().getName())
				.patientPhone(a.getPatient().getPhone()).patientAge(a.getPatient().getAge())
				.patientGender(a.getPatient().getGender()).noshowScore(a.getPatient().getNoshowScore())
				.reason(a.getReason()).scheduledAt(a.getScheduledAt()).status(a.getStatus()).smsSent(a.getSmsSent())
				.notes(a.getNotes()).totalVisits(totalVisits).createdAt(a.getCreatedAt()).build();
	}
}