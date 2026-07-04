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
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        assertInstanceOf(ScrapingAntBadRequestException.class, ex);
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
        assertInstanceOf(ScrapingAntRateLimitException.class, ex);
        ScrapingAntRateLimitException rateLimitException = (ScrapingAntRateLimitException) ex;
        assertEquals(120, rateLimitException.getRetryAfter());
        assertEquals("Slow down", rateLimitException.getMessage());
    }

    @Test
    void decode429HandlesMissingRetryAfterHeader() {
        Response resp = buildResponse(429, null, null);
        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntRateLimitException.class, ex);
        assertNull(((ScrapingAntRateLimitException) ex).getRetryAfter());
    }

    @Test
    void decode429HandlesMalformedRetryAfterHeader() {
        Response resp = buildResponse(429, Map.of("Retry-After", List.of("abc")), null);
        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntRateLimitException.class, ex);
        assertNull(((ScrapingAntRateLimitException) ex).getRetryAfter());
    }

    @Test
    void decodeMalformedBodyUsesFallbackMessageNoNpe() {
        Response resp = buildResponse(400, null, "{ not json");
        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntBadRequestException.class, ex);
        assertEquals(400, ((ScrapingAntBadRequestException) ex).getStatusCode());
        assertThat(ex.getMessage()).contains("Wrong request format");
    }

    @Test
    void privateHelpersReturnNullForNullResponse() throws Exception {
        Method extractDetail = ScrapingAntErrorDecoder.class.getDeclaredMethod("extractDetail", Response.class);
        extractDetail.setAccessible(true);
        Method extractRetryAfter = ScrapingAntErrorDecoder.class.getDeclaredMethod("extractRetryAfter", Response.class);
        extractRetryAfter.setAccessible(true);

        assertNull(extractDetail.invoke(decoder, new Object[] {null}));
        assertNull(extractRetryAfter.invoke(decoder, new Object[] {null}));
    }

    @Test
    void decodeUnknownStatusWithDetailReturnsDetail() {
        Response resp = buildResponse(418, null, "{\"detail\":\"I am a teapot\"}");
        Exception ex = decoder.decode("m", resp);
        assertEquals("I am a teapot", ex.getMessage());
    }

    private static Stream<Arguments> errorStatusProvider() {
        return Stream.of(
                Arguments.of(400, ScrapingAntBadRequestException.class, "Wrong request format. Make sure that you're using a proper JSON input."),
                Arguments.of(403, ScrapingAntForbiddenException.class, "The API token is wrong or you have exceeded the API credits limit."),
                Arguments.of(404, ScrapingAntNotFoundException.class, "The requested URL is not reachable. Please, check it in your browser or try again."),
                Arguments.of(405, ScrapingAntMethodNotAllowedException.class, "The API endpoint can only be accessed using the following HTTP methods: GET, POST, PUT, DELETE."),
                Arguments.of(409, ScrapingAntConflictException.class, "Concurrent requests limit exceeded. Please, try again or upgrade to the paid plan"),
                Arguments.of(422, ScrapingAntUnprocessableEntityException.class, "Invalid value provided. Please, look into detail for more info."),
                Arguments.of(423, ScrapingAntLockedException.class, "The anti-bot detection system has detected the request. Please, retry or change the request settings."),
                Arguments.of(429, ScrapingAntRateLimitException.class, "Too many requests. Please retry later."),
                Arguments.of(500, ScrapingAntServerException.class, "Something went wrong with the server side code. Rare case. We're recommending to contact us in this case.")
        );
    }
}
