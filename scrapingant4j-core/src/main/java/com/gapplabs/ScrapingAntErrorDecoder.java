package com.gapplabs;

import com.gapplabs.constants.ScrapingAntResponseConstants;
import com.gapplabs.exceptions.ScrapingAntHttpException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Feign ErrorDecoder that converts ScrapingAnt API error responses to exceptions. */
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
    if (status == 429) {
      return new ScrapingAntHttpException(status, message, parseRetryAfter(response));
    }
    return new ScrapingAntHttpException(status, message);
  }

  private Duration parseRetryAfter(Response response) {
    String value =
        response.headers().entrySet().stream()
            .filter(
                entry -> ScrapingAntResponseConstants.RETRY_AFTER.equalsIgnoreCase(entry.getKey()))
            .flatMap(entry -> entry.getValue().stream())
            .findFirst()
            .orElse(null);
    if (value == null) {
      return null;
    }
    try {
      long seconds = Long.parseLong(value.trim());
      return seconds < 0 ? null : Duration.ofSeconds(seconds);
    } catch (NumberFormatException ignored) {
      try {
        Instant retryAt =
            ZonedDateTime.parse(value.trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        Duration delay = Duration.between(Instant.now(), retryAt);
        return delay.isNegative() ? Duration.ZERO : delay;
      } catch (DateTimeParseException | ArithmeticException ignoredDate) {
        return null;
      }
    }
  }

  private String extractDetail(Response response) {
    if (response.body() == null) {
      return null;
    }

    try (InputStreamReader reader =
        new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8)) {
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
