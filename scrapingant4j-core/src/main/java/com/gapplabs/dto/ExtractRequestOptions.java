package com.gapplabs.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
@ToString
public class ExtractRequestOptions {

    private final String extractProperties;

    @SuppressWarnings("unused")
    public static class ExtractRequestOptionsBuilder {
        public ExtractRequestOptions build() {
            return new ExtractRequestOptions(normalize(this.extractProperties));
        }
    }

    public static ExtractRequestOptions fromString(String extractProperties) {
        return ExtractRequestOptions.builder()
                .extractProperties(extractProperties)
                .build();
    }

    public static ExtractRequestOptions fromList(List<String> extractProperties) {
        if (extractProperties == null || extractProperties.isEmpty()) {
            throw new IllegalArgumentException("'extractProperties' list is required and cannot be null or empty");
        }
        return fromString(String.join(", ", extractProperties));
    }

    public Map<String, Object> toQueryMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("extract_properties", extractProperties);
        return params;
    }

    private static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("'extractProperties' is required and cannot be blank");
        }

        String normalized = List.of(raw.split(","))
                .stream()
                .map(String::trim)
                .map(token -> token.replaceAll("\\s+", " "))
                .peek(token -> {
                    if (token.isBlank()) {
                        throw new IllegalArgumentException("'extractProperties' contains an empty token");
                    }
                })
                .collect(Collectors.joining(", "));

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("'extractProperties' is required and cannot be blank");
        }

        return normalized;
    }
}
