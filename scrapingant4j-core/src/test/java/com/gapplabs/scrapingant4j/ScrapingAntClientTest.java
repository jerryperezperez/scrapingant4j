package com.gapplabs.scrapingant4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gapplabs.scrapingant4j.dto.ScrapingAntRequest;
import com.gapplabs.scrapingant4j.dto.responses.ExtendedResponse;
import com.gapplabs.scrapingant4j.dto.responses.MarkdownResponse;
import com.gapplabs.scrapingant4j.exceptions.ScrapingAntResponseException;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ScrapingAntClientTest {

  private ScrapingAntClient client;

  @Mock private ScrapingAntApi mockApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ScrapingAntClientOptions options =
        ScrapingAntClientOptions.builder().apiKey("test-api-key").build();
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
    String jsonResponse =
        """
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
    assertEquals("Content-Type", result.headers().getFirst().name());
    assertEquals(201, result.xhrs().getFirst().status());
    assertEquals("application/json", result.xhrs().getFirst().headers().getFirst().value());
    assertEquals("{}", result.xhrs().getFirst().requestBody());
    assertEquals("https://example.com/frame", result.iframes().getFirst().src());
    assertFalse(result.toString().contains("Content-Type"));
  }

  @Test
  void testExecuteMarkdownRejectsMalformedJson() {
    ScrapingAntRequest request = createBasicRequest();
    when(mockApi.scrape(any(), eq("markdown"))).thenReturn("not json");

    ScrapingAntResponseException exception =
        assertThrows(ScrapingAntResponseException.class, () -> client.executeMarkdown(request));

    org.junit.jupiter.api.Assertions.assertNotNull(exception.getCause());
  }

  @Test
  void testExecuteMarkdownRejectsJsonNull() {

    ScrapingAntRequest request = createBasicRequest();
    when(mockApi.scrape(any(), eq("markdown"))).thenReturn("null");
    assertThrows(ScrapingAntResponseException.class, () -> client.executeMarkdown(request));
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
  void testExecuteExtractCanDeserializeIntoAType() {
    when(mockApi.scrape(any(), eq("extract"))).thenReturn("{\"title\":\"Example\"}");

    Extracted result = client.executeExtract(createBasicRequest(), "title", Extracted.class);

    assertEquals("Example", result.title());
  }

  @Test
  void testExecuteExtractCanDeserializeParameterizedTypes() {
    when(mockApi.scrape(any(), eq("extract"))).thenReturn("[{\"title\":\"Example\"}]");

    List<Extracted> result =
        client.executeExtract(
            createBasicRequest(), "title", new TypeToken<List<Extracted>>() {}.getType());

    assertEquals("Example", result.getFirst().title());
  }

  private ScrapingAntRequest createBasicRequest() {
    return ScrapingAntRequest.builder().url("https://example.com").build();
  }

  private record Extracted(String title) {}
}
