package com.careos.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.careos.api.dto.FollowupResponse;
import com.careos.api.model.Appointment;
import com.careos.api.model.Followup;
import com.careos.api.model.Patient;
import com.careos.api.repository.AppointmentRepository;
import com.careos.api.repository.DoctorRepository;
import com.careos.api.repository.FollowupRepository;
import com.careos.api.repository.PatientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowupService {

	private final FollowupRepository followupRepository;
	private final PatientRepository patientRepository;
	private final AppointmentRepository appointmentRepository;
	private final DoctorRepository doctorRepository;
	private final AiService aiService;
	private final SmsService smsService;

	// get all draft followups for a doctor
	public List<FollowupResponse> getDraftFollowups(UUID doctorId) {
		return followupRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctorId, "draft").stream()
				.map(this::mapToResponse).collect(Collectors.toList());
	}

	// doctor approves and sends a followup
	public FollowupResponse sendFollowup(UUID doctorId, UUID followupId, String finalMessage) {
		Followup followup = followupRepository.findById(followupId)
				.orElseThrow(() -> new RuntimeException("Followup not found"));

		if (!followup.getDoctor().getId().equals(doctorId)) {
			throw new RuntimeException("Access denied");
		}

		String messageToSend = finalMessage != null ? finalMessage : followup.getAiMessage();

		String doctorName = followup.getDoctor().getName();
		String fullMessage = smsService.buildFollowupMessage(followup.getPatient().getName(), doctorName,
				messageToSend);

		boolean sent = smsService.sendSms(followup.getPatient().getPhone(), fullMessage);

		if (sent) {
			followup.setFinalMessage(messageToSend);
			followup.setStatus("sent");
			followup.setSentAt(LocalDateTime.now());
			log.info("Followup sent to patient: {}", followup.getPatient().getName());
		}

		return mapToResponse(followupRepository.save(followup));
	}

	// doctor skips a followup
	public FollowupResponse skipFollowup(UUID doctorId, UUID followupId) {
		Followup followup = followupRepository.findById(followupId)
				.orElseThrow(() -> new RuntimeException("Followup not found"));

		if (!followup.getDoctor().getId().equals(doctorId)) {
			throw new RuntimeException("Access denied");
		}

		followup.setStatus("skipped");
		return mapToResponse(followupRepository.save(followup));
	}

	// runs every day at 8am — generates AI followup drafts
	@Scheduled(cron = "0 0 8 * * *")
	public void generateDailyFollowups() {
		log.info("Running daily followup generation...");

		List<Patient> allPatients = patientRepository.findAll();

		for (Patient patient : allPatients) {
			try {
				processPatientForFollowup(patient);
			} catch (Exception e) {
				log.error("Error processing patient {}: {}", patient.getName(), e.getMessage());
			}
		}
	}

	// runs every day at 9am — sends SMS reminders for tomorrow's appts
	@Scheduled(cron = "0 0 9 * * *")
	public void sendAppointmentReminders() {
		log.info("Sending appointment reminders...");

		LocalDateTime from = LocalDateTime.now().plusHours(23);
		LocalDateTime to = LocalDateTime.now().plusHours(25);

		List<Appointment> upcoming = appointmentRepository.findAppointmentsNeedingReminder(from, to);

		for (Appointment appt : upcoming) {
			try {
				String bookingLink = smsService.buildBookingLink(appt.getDoctor().getId().toString());
				String message = smsService.buildReminderMessage(appt.getPatient().getName(),
						appt.getDoctor().getName(), appt.getScheduledAt().toString(), bookingLink);
				boolean sent = smsService.sendSms(appt.getPatient().getPhone(), message);
				if (sent) {
					appt.setSmsSent(true);
					appointmentRepository.save(appt);
				}
			} catch (Exception e) {
				log.error("Reminder failed for appointment {}: {}", appt.getId(), e.getMessage());
			}
		}
	}

	private void processPatientForFollowup(Patient patient) {
		// skip if draft already exists
		if (followupRepository.existsByPatientIdAndStatus(patient.getId(), "draft")) {
			return;
		}

		// get last appointment
		List<Appointment> appointments = appointmentRepository
				.findByPatientIdAndStatusOrderByScheduledAtAsc(patient.getId(), "done");

		if (appointments.isEmpty())
			return;

		Appointment lastVisit = appointments.get(appointments.size() - 1);
		long daysSince = ChronoUnit.DAYS.between(lastVisit.getScheduledAt().toLocalDate(), LocalDate.now());

		// check no upcoming appointment already booked
		List<Appointment> upcoming = appointmentRepository
				.findByPatientIdAndStatusOrderByScheduledAtAsc(patient.getId(), "upcoming");
		if (!upcoming.isEmpty())
			return;

		// trigger followup if overdue (>14 days) or high noshow risk
		String triggerReason = null;
		if (daysSince > 30) {
			triggerReason = "overdue";
		} else if (patient.getNoshowScore() >= 60 && daysSince > 14) {
			triggerReason = "noshow_risk";
		}

		if (triggerReason == null)
			return;

		// generate AI message
		String condition = patient.getConditions() != null && !patient.getConditions().isEmpty()
				? patient.getConditions().get(0)
				: "general checkup";

		String medication = patient.getMedications() != null && !patient.getMedications().isEmpty()
				? patient.getMedications().get(0)
				: "prescribed medication";

		String aiMessage = aiService.generateFollowupMessage(patient.getName(), condition, medication,
				String.valueOf(daysSince), patient.getDoctor().getName(), triggerReason);

		Followup followup = new Followup();
		followup.setDoctor(patient.getDoctor());
		followup.setPatient(patient);
		followup.setAiMessage(aiMessage);
		followup.setTriggerReason(triggerReason);
		followup.setStatus("draft");

		followupRepository.save(followup);
		log.info("Followup draft created for patient: {}", patient.getName());
	}

	private FollowupResponse mapToResponse(Followup f) {
		return FollowupResponse.builder().id(f.getId()).patientId(f.getPatient().getId())
				.patientName(f.getPatient().getName()).patientPhone(f.getPatient().getPhone())
				.aiMessage(f.getAiMessage()).finalMessage(f.getFinalMessage()).status(f.getStatus())
				.triggerReason(f.getTriggerReason()).noshowScore(f.getPatient().getNoshowScore()).sentAt(f.getSentAt())
				.createdAt(f.getCreatedAt()).build();
	}
}