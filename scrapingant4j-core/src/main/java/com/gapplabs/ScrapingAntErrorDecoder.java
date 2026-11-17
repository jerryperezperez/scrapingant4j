package com.gapplabs;

import com.gapplabs.exceptions.ScrapingAntException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Feign ErrorDecoder that converts ScrapingAnt API error responses to exceptions.
 */
class ScrapingAntErrorDecoder implements ErrorDecoder {

    private static final String DETAIL_FIELD = "detail";
    private final Gson gson = new Gson();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response == null) {
            return new IllegalArgumentException("No response received from the server.");
        }

        int status = response.status();
        String detail = extractDetail(response);
        String message = detail == null ? "HTTP " + status : detail;
        return new ScrapingAntException(status, message);
    }

    private String extractDetail(Response response) {
        if (response.body() == null) {
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(
                response.body().asInputStream(), StandardCharsets.UTF_8)) {
            JsonElement json = gson.fromJson(reader, JsonElement.class);
            if (json == null || !json.isJsonObject()) {
                return null;
            }

            JsonElement detail = json.getAsJsonObject().get(DETAIL_FIELD);
            if (detail == null || !detail.isJsonPrimitive() || !detail.getAsJsonPrimitive().isString()) {
                return null;
            }

            String value = detail.getAsString();
            return value.isBlank() ? null : value;
        } catch (IOException | JsonParseException ignored) {
            return null;
        }
    }
}
