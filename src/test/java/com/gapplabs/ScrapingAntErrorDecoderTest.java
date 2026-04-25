package com.gapplabs;

import com.gapplabs.exceptions.ScrapingAntException;
import com.gapplabs.exceptions.ScrapingAntForbiddenException;
import com.gapplabs.exceptions.ScrapingAntNotFoundException;
import com.gapplabs.exceptions.ScrapingAntRateLimitException;
import com.gapplabs.exceptions.ScrapingAntServerException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScrapingAntErrorDecoderTest {

    private final ScrapingAntErrorDecoder decoder = new ScrapingAntErrorDecoder();

    private Response buildResponse(int status, Map<String, Collection<String>> headers, String body) {
        byte[] bytes = null;
        if (body != null) {
            bytes = body.getBytes(StandardCharsets.UTF_8);
        }
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost", Collections.emptyMap(), null, null, null);
        return Response.builder()
                .status(status)
                .reason("Reason")
                .headers(headers != null ? headers : Collections.emptyMap())
                .request(request)
                .body(bytes)
                .build();
    }

    @Test
    void decode403ThrowsForbidden() {
        Response resp = buildResponse(403, null, "{\"detail\":\"Forbidden\"}");

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntForbiddenException.class, ex);
        assertEquals(403, ((ScrapingAntForbiddenException) ex).getStatusCode());
    }

    @Test
    void decode404ThrowsNotFound() {
        Response resp = buildResponse(404, null, "{\"detail\":\"Not found\"}");

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntNotFoundException.class, ex);
        assertEquals(404, ((ScrapingAntNotFoundException) ex).getStatusCode());
    }

    @Test
    void decode429ThrowsRateLimitAndReadsRetryAfter() {
        Response resp = buildResponse(429, Map.of("Retry-After", List.of("120")), "{\"detail\":\"Too many requests\"}");

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntRateLimitException.class, ex);
        ScrapingAntRateLimitException rateLimitException = (ScrapingAntRateLimitException) ex;
        assertEquals(429, rateLimitException.getStatusCode());
        assertEquals(120, rateLimitException.getRetryAfter());
    }

    @Test
    void decode500ThrowsServer() {
        Response resp = buildResponse(500, null, "{\"detail\":\"Server error\"}");

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntServerException.class, ex);
        assertEquals(500, ((ScrapingAntServerException) ex).getStatusCode());
    }

    @Test
    void decode422ThrowsBase() {
        Response resp = buildResponse(422, null, "{\"detail\":\"Unprocessable\"}");

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
        assertEquals(422, ((ScrapingAntException) ex).getStatusCode());
    }

    @Test
    void decodeMalformedBodyUsesFallbackMessageNoNpe() {
        Response resp = buildResponse(400, null, "{ not json");

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
        assertEquals(400, ((ScrapingAntException) ex).getStatusCode());
        assertNotNull(ex.getMessage());
    }
}
