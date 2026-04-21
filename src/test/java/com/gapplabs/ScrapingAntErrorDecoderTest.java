package com.gapplabs;

import com.gapplabs.exceptions.*;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    void decode_403_throwsForbidden() {
        String detail = "{\"detail\":\"Forbidden\"}";
        Response resp = buildResponse(403, null, detail);

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntForbiddenException.class, ex);
        assertEquals(403, ((ScrapingAntForbiddenException) ex).getStatusCode());
    }

    @Test
    void decode_404_throwsNotFound() {
        String detail = "{\"detail\":\"Not found\"}";
        Response resp = buildResponse(404, null, detail);

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntNotFoundException.class, ex);
        assertEquals(404, ((ScrapingAntNotFoundException) ex).getStatusCode());
    }

    @Test
    void decode_500_throwsServer() {
        String detail = "{\"detail\":\"Server error\"}";
        Response resp = buildResponse(500, null, detail);

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntServerException.class, ex);
        assertEquals(500, ((ScrapingAntServerException) ex).getStatusCode());
    }

    @Test
    void decode_422_throwsBase() {
        String detail = "{\"detail\":\"Unprocessable\"}";
        Response resp = buildResponse(422, null, detail);

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
        assertEquals(422, ((ScrapingAntException) ex).getStatusCode());
    }

    @Test
    void decode_malformedBody_usesFallbackMessage_noNpe() {
        String bad = "{ not json";
        Response resp = buildResponse(400, null, bad);

        Exception ex = decoder.decode("m", resp);
        assertInstanceOf(ScrapingAntException.class, ex);
        assertEquals(400, ((ScrapingAntException) ex).getStatusCode());
        assertNotNull(ex.getMessage());
    }
}
