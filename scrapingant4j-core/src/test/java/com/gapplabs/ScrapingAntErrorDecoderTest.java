package com.gapplabs;

import com.gapplabs.exceptions.ScrapingAntException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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

    @ParameterizedTest
    @MethodSource("errorStatusProvider")
    void testSpecificErrorCodes(
            int status,
            Class<? extends ScrapingAntException> expectedExceptionClass,
            String expectedDefaultMessage
    ) {
        Response resp = buildResponse(status, null, null);
        Exception ex = decoder.decode("m", resp);

        assertInstanceOf(expectedExceptionClass, ex);
        ScrapingAntException sae = (ScrapingAntException) ex;
        assertEquals(status, sae.getStatusCode());
        assertEquals(expectedDefaultMessage, sae.getMessage());
    }

    @Test
    void decode400UsesDetailWhenPresent() {
        Response resp = buildResponse(400, null, "{\"detail\":\"Bad request detail\"}");

        Exception ex = decoder.decode("m", resp);

        assertInstanceOf(ScrapingAntException.class, ex);
        assertEquals("Bad request detail", ex.getMessage());
    }

    @Test
    void decodeUnknownStatusReturnsBaseException() {
        Response resp = buildResponse(418, null, null);

        Exception ex = decoder.decode("m", resp);

        assertInstanceOf(ScrapingAntException.class, ex);
        assertEquals(418, ((ScrapingAntException) ex).getStatusCode());
        assertEquals("HTTP 418", ex.getMessage());
    }

    @Test
    void decodeUnknownStatusWithEmptyDetailReturnsFallbackMessage() {
        Response resp = buildResponse(418, null, "{\"detail\":\"\"}");

        Exception ex = decoder.decode("m", resp);

        assertEquals("HTTP 418", ex.getMessage());
    }

    @Test
    void decodeUnknownStatusWithNullDetailFieldReturnsFallbackMessage() {
        Response resp = buildResponse(418, null, "{\"detail\":null}");

        Exception ex = decoder.decode("m", resp);

        assertEquals("HTTP 418", ex.getMessage());
    }

    @Test
    void decodeUnknownStatusWithUnrelatedJsonReturnsFallbackMessage() {
        Response resp = buildResponse(418, null, "{\"foo\":\"bar\"}");

        Exception ex = decoder.decode("m", resp);

        assertEquals("HTTP 418", ex.getMessage());
    }

    @Test
    void decode429ReadsRetryAfterHeader() {
        Response resp = buildResponse(429, Map.of("Retry-After", List.of("120")), "{\"detail\":\"Slow down\"}");

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
        ScrapingAntException rateLimitException = (ScrapingAntException) ex;
        assertEquals("Slow down", rateLimitException.getMessage());
    }

    @Test
    void decode429HandlesMissingRetryAfterHeader() {
        Response resp = buildResponse(429, null, null);
        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
    }

    @Test
    void decode429HandlesMalformedRetryAfterHeader() {
        Response resp = buildResponse(429, Map.of("Retry-After", List.of("abc")), null);
        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
    }

    @Test
    void decodeMalformedBodyUsesFallbackMessageNoNpe() {
        Response resp = buildResponse(400, null, "{ not json");
        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
        assertEquals(400, ((ScrapingAntException) ex).getStatusCode());
        assertEquals("HTTP 400", ex.getMessage());
    }

    @Test
    void decodeUnknownStatusWithDetailReturnsDetail() {
        Response resp = buildResponse(418, null, "{\"detail\":\"I am a teapot\"}");
        Exception ex = decoder.decode("m", resp);
        assertEquals("I am a teapot", ex.getMessage());
    }

    private static Stream<Arguments> errorStatusProvider() {
        return Stream.of(
                Arguments.of(400, ScrapingAntException.class, "HTTP 400"),
                Arguments.of(403, ScrapingAntException.class, "HTTP 403"),
                Arguments.of(404, ScrapingAntException.class, "HTTP 404"),
                Arguments.of(405, ScrapingAntException.class, "HTTP 405"),
                Arguments.of(409, ScrapingAntException.class, "HTTP 409"),
                Arguments.of(422, ScrapingAntException.class, "HTTP 422"),
                Arguments.of(423, ScrapingAntException.class, "HTTP 423"),
                Arguments.of(429, ScrapingAntException.class, "HTTP 429"),
                Arguments.of(500, ScrapingAntException.class, "HTTP 500")
        );
    }
}
