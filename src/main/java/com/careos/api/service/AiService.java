package com.careos.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {

	@Value("${openai.api.key}")
	private String apiKey;

	@Value("${openai.model}")
	private String model;

	private final RestTemplate restTemplate = new RestTemplate();

	private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

	public String generateFollowupMessage(String patientName, String condition, String medication,
			String daysSinceVisit, String doctorName, String triggerReason) {
		String prompt = String.format("""
				You are an assistant for an Indian clinic doctor.
				Write a short, warm SMS follow-up message in English
				for the doctor to send to their patient.

				Patient name: %s
				Condition: %s
				Medication: %s
				Days since last visit: %s
				Doctor name: %s
				Reason for follow-up: %s

				Rules:
				- Maximum 2 sentences
				- Warm and personal tone
				- Address patient as '%s ji'
				- Mention condition or medication naturally
				- End with a call to action to book appointment
				- Do NOT include doctor signature, it will be added separately
				- Write in English only
				""", patientName, condition, medication, daysSinceVisit, doctorName, triggerReason, patientName);

		return callOpenAi(prompt);
	}

	public String structureVisitNotes(String rawNotes, String patientName, String condition) {
		String prompt = String.format("""
				Structure these raw doctor visit notes into a clean
				SOAP format (Subjective, Objective, Assessment, Plan).

				Patient: %s
				Known condition: %s
				Raw notes: %s

				Return clean structured notes only, no extra text.
				""", patientName, condition, rawNotes);

		return callOpenAi(prompt);
	}

	public Integer computeNoshowScore(String patientName, int missedAppointments, int totalAppointments,
			int daysSinceLastVisit) {
		String prompt = String.format("""
				Compute a no-show risk score from 0 to 100 for this patient.
				Higher score means more likely to miss next appointment.

				Patient: %s
				Missed appointments: %d
				Total appointments: %d
				Days since last visit: %d

				Return ONLY a single integer number between 0 and 100.
				Nothing else.
				""", patientName, missedAppointments, totalAppointments, daysSinceLastVisit);

		try {
			String result = callOpenAi(prompt).trim();
			return Integer.parseInt(result);
		} catch (Exception e) {
			log.error("Failed to parse noshow score: {}", e.getMessage());
			return 0;
		}
	}

	private String callOpenAi(String prompt) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(apiKey);

			Map<String, Object> message = new HashMap<>();
			message.put("role", "user");
			message.put("content", prompt);

			Map<String, Object> body = new HashMap<>();
			body.put("model", model);
			body.put("messages", List.of(message));
			body.put("max_tokens", 200);
			body.put("temperature", 0.7);

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

			ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);

			Map choices = (Map) ((List) response.getBody().get("choices")).get(0);
			Map messageResponse = (Map) choices.get("message");
			return (String) messageResponse.get("content");

		} catch (Exception e) {
			log.error("OpenAI call failed: {}", e.getMessage());
			return "Unable to generate message. Please write manually.";
		}
	}
}