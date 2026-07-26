package com.gapplabs;

import com.gapplabs.dto.ExtractRequestOptions;
import com.gapplabs.dto.ScrapingAntRequest;
import com.gapplabs.dto.responses.ExtendedResponse;
import com.gapplabs.dto.responses.MarkdownResponse;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        String jsonResponse = "{\"html\":\"<html></html>\", \"status_code\":200}";
        when(mockApi.scrape(any(), eq("extended"))).thenReturn(jsonResponse);

        ExtendedResponse result = client.executeExtended(request);

        assertEquals(200, result.statusCode());
    }

    @Test
    void testExecuteExtractTyped() {
        ScrapingAntRequest request = createBasicRequest();
        String jsonResponse = "{\"price\":99.5,\"currency\":\"USD\"}";
        when(mockApi.scrape(any(), eq("extract"))).thenReturn(jsonResponse);

        Map<String, Object> result = client.executeExtract(request);

        assertEquals(99.5, result.get("price"));
        assertEquals("USD", result.get("currency"));
    }

    @Test
    void testExecuteMarkdownRejectsMalformedJson() {
        ScrapingAntRequest request = createBasicRequest();
        when(mockApi.scrape(any(), eq("markdown"))).thenReturn("not json");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> client.executeMarkdown(request));

        assertInstanceOf(JsonSyntaxException.class, exception.getCause());
    }

    @Test
    void testExecuteExtractWithOptionsAddsExtractProperties() {
        ScrapingAntRequest request = createBasicRequest();
        ExtractRequestOptions options = ExtractRequestOptions.fromString("title, price");
        String jsonResponse = "{\"title\":\"A\",\"price\":99.5}";
        when(mockApi.scrape(any(), eq("extract"))).thenReturn(jsonResponse);

        client.executeExtract(request, options);

        ArgumentCaptor<Map<String, Object>> queryCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockApi).scrape(queryCaptor.capture(), eq("extract"));
        Map<String, Object> queryMap = queryCaptor.getValue();
        assertEquals("https://example.com", queryMap.get("url"));
        assertEquals("title, price", queryMap.get("extract_properties"));
    }

    @Test
    void testPublicStringConstructorBuildsClientOptions() {
        ScrapingAntClient configuredClient = new ScrapingAntClient("test-api-key");

        assertEquals("test-api-key", configuredClient.getOptions().getApiKey());
        assertEquals(ScrapingAntClientOptions.DEFAULT_ENDPOINT, configuredClient.getOptions().getEndpoint());
        assertNotNull(configuredClient.getOptions().getFeignBuilder());
    }

    @Test
    void testPublicOptionsConstructorBuildsFeignTarget() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .endpoint("https://custom.scrapingant.com/")
                .apiVersion("/v3")
                .build();

        ScrapingAntClient configuredClient = new ScrapingAntClient(options);

        assertSame(options, configuredClient.getOptions());
        assertEquals("https://custom.scrapingant.com/v3", configuredClient.getOptions().getBaseUrl());
    }

    @Test
    void testConvertResponseRejectsUnknownExtractionType() throws Exception {
        Method convertResponse = ScrapingAntClient.class.getDeclaredMethod(
                "convertResponse",
                String.class,
                com.gapplabs.constants.ExtractionType.class
        );
        convertResponse.setAccessible(true);

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> convertResponse.invoke(client, "{}", (Object) null)
        );

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
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
                .endpoint("https://custom.scrapingant.com/")
                .apiVersion("/v3")
                .build();

        ScrapingAntClient configuredClient = new ScrapingAntClient(options, mockApi);

        assertSame(options, configuredClient.getOptions());
        assertEquals("https://custom.scrapingant.com/v3", configuredClient.getOptions().getBaseUrl());
    }

    private ScrapingAntRequest createBasicRequest() {
        return ScrapingAntRequest.builder()
                .url("https://example.com")
                .build();
    }
}
