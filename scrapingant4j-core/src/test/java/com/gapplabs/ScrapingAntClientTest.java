package com.gapplabs;

import com.gapplabs.dto.ScrapingAntRequest;
import com.gapplabs.dto.responses.ExtendedResponse;
import com.gapplabs.dto.responses.MarkdownResponse;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScrapingAntClientTest {

    private ScrapingAntClient client;

    @Mock
    private ScrapingAntApi mockApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .build();
        client = new ScrapingAntClient(options, mockApi);
    }

    @Test
    void testExecuteGeneralTyped() {
        ScrapingAntRequest request = createBasicRequest();

        String expectedResponse = "<html>some html</html>";
        when(mockApi.scrape(any(), eq("general"))).thenReturn(expectedResponse);

        String result = client.executeGeneral(request);

        assertEquals(expectedResponse, result);
    }

    @Test
    void testExecuteMarkdownTyped() {
        ScrapingAntRequest request = createBasicRequest();
        String jsonResponse = "{\"markdown\":\"# Hello\", \"html\":\"<h1>Hello</h1>\"}";
        when(mockApi.scrape(any(), eq("markdown"))).thenReturn(jsonResponse);

        MarkdownResponse result = client.executeMarkdown(request);

        assertEquals("# Hello", result.markdown());
    }

    @Test
    void testExecuteExtendedTyped() {
        ScrapingAntRequest request = createBasicRequest();
        String jsonResponse = """
                {
                  "html": "<html></html>",
                  "status_code": 200,
                  "headers": [{"name": "Content-Type", "value": "text/html"}],
                  "xhrs": [{
                    "url": "https://example.com/api",
                    "status": 201,
                    "method": "POST",
                    "headers": [{"name": "Content-Type", "value": "application/json"}],
                    "request_body": "{}",
                    "body": "{\\"ok\\":true}"
                  }],
                  "iframes": [{"src": "https://example.com/frame", "html": "<p>frame</p>"}]
                }
                """;
        when(mockApi.scrape(any(), eq("extended"))).thenReturn(jsonResponse);

        ExtendedResponse result = client.executeExtended(request);

        assertEquals(200, result.statusCode());
        assertEquals("Content-Type", result.headers().get(0).name());
        assertEquals(201, result.xhrs().get(0).status());
        assertEquals("application/json", result.xhrs().get(0).headers().get(0).value());
        assertEquals("{}", result.xhrs().get(0).requestBody());
        assertEquals("https://example.com/frame", result.iframes().get(0).src());
    }

    @Test
    void testExecuteMarkdownRejectsMalformedJson() {
        ScrapingAntRequest request = createBasicRequest();
        when(mockApi.scrape(any(), eq("markdown"))).thenReturn("not json");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> client.executeMarkdown(request));

        assertInstanceOf(JsonSyntaxException.class, exception.getCause());
    }

    @Test
    void testPublicStringConstructorBuildsClientOptions() {
        ScrapingAntClient configuredClient = new ScrapingAntClient("test-api-key");

        assertEquals("test-api-key", configuredClient.getOptions().getApiKey());
        assertEquals(ScrapingAntClientOptions.BASE_URL, configuredClient.getOptions().getBaseUrl());
        assertNotNull(configuredClient.getOptions().getFeignBuilder());
    }

    @Test
    void testPublicOptionsConstructorBuildsFeignTarget() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .build();

        ScrapingAntClient configuredClient = new ScrapingAntClient(options);

        assertSame(options, configuredClient.getOptions());
        assertEquals(ScrapingAntClientOptions.BASE_URL, configuredClient.getOptions().getBaseUrl());
    }

    @Test
    void testExecuteExtractWithStringDelegatesToOptionsPath() {
        ScrapingAntRequest request = createBasicRequest();
        String jsonResponse = "{\"title\":\"A\"}";
        when(mockApi.scrape(any(), eq("extract"))).thenReturn(jsonResponse);

        client.executeExtract(request, " title  ,   price ");

        ArgumentCaptor<Map<String, Object>> queryCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockApi).scrape(queryCaptor.capture(), eq("extract"));
        assertEquals("title, price", queryCaptor.getValue().get("extract_properties"));
    }

    @Test
    void testExecuteExtractWithListDelegatesToOptionsPath() {
        ScrapingAntRequest request = createBasicRequest();
        String jsonResponse = "{\"title\":\"A\"}";
        when(mockApi.scrape(any(), eq("extract"))).thenReturn(jsonResponse);

        client.executeExtract(request, List.of("title", "price"));

        ArgumentCaptor<Map<String, Object>> queryCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockApi).scrape(queryCaptor.capture(), eq("extract"));
        assertEquals("title, price", queryCaptor.getValue().get("extract_properties"));
    }

    @Test
    void testClientRetainsConfiguredOptions() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .build();

        ScrapingAntClient configuredClient = new ScrapingAntClient(options, mockApi);

        assertSame(options, configuredClient.getOptions());
        assertEquals(ScrapingAntClientOptions.BASE_URL, configuredClient.getOptions().getBaseUrl());
    }

    private ScrapingAntRequest createBasicRequest() {
        return ScrapingAntRequest.builder()
                .url("https://example.com")
                .build();
    }
}
