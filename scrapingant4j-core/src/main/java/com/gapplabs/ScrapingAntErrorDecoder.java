package com.gapplabs;

import com.gapplabs.exceptions.ScrapingAntBadRequestException;
import com.gapplabs.exceptions.ScrapingAntConflictException;
import com.gapplabs.exceptions.ScrapingAntException;
import com.gapplabs.exceptions.ScrapingAntForbiddenException;
import com.gapplabs.exceptions.ScrapingAntLockedException;
import com.gapplabs.exceptions.ScrapingAntMethodNotAllowedException;
import com.gapplabs.exceptions.ScrapingAntNotFoundException;
import com.gapplabs.exceptions.ScrapingAntRateLimitException;
import com.gapplabs.exceptions.ScrapingAntServerException;
import com.gapplabs.exceptions.ScrapingAntUnprocessableEntityException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Feign ErrorDecoder that converts ScrapingAnt API error responses to typed exceptions.
 */
class ScrapingAntErrorDecoder implements ErrorDecoder {

    private final Gson gson = new Gson();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String detail = extractDetail(response);

        switch (status) {
            case 400:
                return new ScrapingAntBadRequestException(status, detail != null ? detail : "Wrong request format. Make sure that you're using a proper JSON input.");
            case 403:
                return new ScrapingAntForbiddenException(status, detail != null ? detail : "The API token is wrong or you have exceeded the API credits limit.");
            case 404:
                return new ScrapingAntNotFoundException(status, detail != null ? detail : "The requested URL is not reachable. Please, check it in your browser or try again.");
            case 405:
                return new ScrapingAntMethodNotAllowedException(status, detail != null ? detail : "The API endpoint can only be accessed using the following HTTP methods: GET, POST, PUT, DELETE.");
            case 409:
                return new ScrapingAntConflictException(status, detail != null ? detail : "Concurrent requests limit exceeded. Please, try again or upgrade to the paid plan");
            case 422:
                return new ScrapingAntUnprocessableEntityException(status, detail != null ? detail : "Invalid value provided. Please, look into detail for more info.");
            case 423:
                return new ScrapingAntLockedException(status, detail != null ? detail : "The anti-bot detection system has detected the request. Please, retry or change the request settings.");
            case 429:
                return new ScrapingAntRateLimitException(
                        status,
                        detail != null ? detail : "Too many requests. Please retry later.",
                        extractRetryAfter(response)
                );
            case 500:
                return new ScrapingAntServerException(status, detail != null ? detail : "Something went wrong with the server side code. Rare case. We're recommending to contact us in this case.");
            default:
                String fallback = "HTTP " + status;
                String message = (detail != null && !detail.isEmpty()) ? detail : fallback;
                return new ScrapingAntException(status, message);
        }
    }

    private String extractDetail(Response response) {
        if (response == null || response.body() == null) {
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8)) {
            JsonObject obj = gson.fromJson(reader, JsonObject.class);
            if (obj != null && obj.has("detail") && !obj.get("detail").isJsonNull()) {
                return obj.get("detail").getAsString();
            }
        } catch (IOException | JsonSyntaxException ignored) {
            // fall back to generic messages
        }
        return null;
    }

    private Integer extractRetryAfter(Response response) {
        if (response == null || response.headers() == null) {
            return null;
        }
        for (Map.Entry<String, Collection<String>> entry : response.headers().entrySet()) {
            if ("Retry-After".equalsIgnoreCase(entry.getKey()) && entry.getValue() != null && !entry.getValue().isEmpty()) {
                String raw = entry.getValue().iterator().next();
                try {
                    return Integer.valueOf(raw);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
