package com.bizmap.store.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AddressValidationService {

    private final RestTemplate restTemplate;
    private final String apiKey;

    public AddressValidationService(
            RestTemplate restTemplate,
            @Value("${google.maps.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public AddressValidationResult validate(String address) {
        try {
            String url = "https://addressvalidation.googleapis.com/v1:validateAddress?key=" + apiKey;

            Map<String, Object> body = Map.of(
                    "address", Map.of("regionCode", "KR", "addressLines", List.of(address)),
                    "languageOptions", Map.of("returnEnglishLatinAddress", false)
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);

            if (response == null || !response.containsKey("result")) {
                return fallback();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.get("result");

            @SuppressWarnings("unchecked")
            Map<String, Object> verdict = (Map<String, Object>) result.get("verdict");

            boolean addressComplete = verdict != null
                    && Boolean.TRUE.equals(verdict.get("addressComplete"));
            boolean hasUnconfirmed = verdict != null
                    && Boolean.TRUE.equals(verdict.get("hasUnconfirmedComponents"));

            @SuppressWarnings("unchecked")
            Map<String, Object> addressObj = (Map<String, Object>) result.get("address");
            String formattedAddress = addressObj != null
                    ? (String) addressObj.get("formattedAddress")
                    : null;

            return new AddressValidationResult(addressComplete, hasUnconfirmed, formattedAddress);
        } catch (Exception e) {
            log.warn("Address validation API 호출 실패, fallback 처리: {}", e.getMessage());
            return fallback();
        }
    }

    private AddressValidationResult fallback() {
        return new AddressValidationResult(true, false, null);
    }

    public record AddressValidationResult(
            boolean isValid,
            boolean hasWarning,
            String normalizedAddress
    ) {}
}
