package com.careos.api.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsService {

	@Value("${msg91.api.key}")
	private String apiKey;

	@Value("${msg91.sender.id}")
	private String senderId;

	@Value("${app.base.url}")
	private String baseUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	public boolean sendSms(String phone, String message) {
		try {
			String url = "https://api.msg91.com/api/v5/flow/";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("authkey", apiKey);

			Map<String, Object> body = new HashMap<>();
			body.put("sender", senderId);
			body.put("route", "4");
			body.put("country", "91");

			Map<String, Object> sms = new HashMap<>();
			sms.put("message", message);
			sms.put("to", new String[] { phone });

			body.put("sms", new Object[] { sms });

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

			log.info("SMS sent to {} — status: {}", phone, response.getStatusCode());
			return response.getStatusCode().is2xxSuccessful();

		} catch (Exception e) {
			log.error("SMS failed to {}: {}", phone, e.getMessage());
			return false;
		}
	}

	public String buildBookingLink(String doctorId) {
		return baseUrl + "/book/" + doctorId;
	}

	public String buildReminderMessage(String patientName, String doctorName, String dateTime, String bookingLink) {
		return String.format(
				"Hi %s, your appointment with %s is confirmed for %s. " + "To reschedule, book here: %s . - CareOS",
				patientName, doctorName, dateTime, bookingLink);
	}

	public String buildFollowupMessage(String patientName, String doctorName, String message) {
		return String.format("%s - %s | CareOS", message, doctorName);
	}
}