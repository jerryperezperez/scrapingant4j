package com.gapplabs.scrapingant4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gapplabs.scrapingant4j.constants.ExtractionType;
import com.gapplabs.scrapingant4j.dto.responses.ExtendedResponse;
import com.gapplabs.scrapingant4j.dto.responses.MarkdownResponse;
import com.gapplabs.scrapingant4j.exceptions.ScrapingAntResponseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScrapingAntResponseConverterTest {

  @Test
  void toMarkdownMapsKnownFieldsAndIgnoresUnknownFields() {
    String raw =
        """
        {"url":"https://example.com","markdown":"# Hello","unknown":42}
        """;

    MarkdownResponse result = ScrapingAntResponseConverter.toMarkdown(raw);

    assertEquals("https://example.com", result.url());
    assertEquals("# Hello", result.markdown());
  }

  @Test
  void toExtendedMapsNestedFields() {
    String raw =
        """
        {
          "html": "<html></html>",
          "text": "hello",
          "cookies": "a=b",
          "status_code": 200,
          "headers": [{"name": "Content-Type", "value": "text/html"}],
          "xhrs": [{
            "url": "https://example.com/api",
            "status": 201,
            "method": "POST",
            "headers": [{"name": "Accept", "value": "application/json"}],
            "request_body": "{}",
            "body": "{\\"ok\\":true}"
          }],
          "iframes": [{"src": "https://example.com/frame", "html": "<p>frame</p>"}]
        }
        """;

    ExtendedResponse result = ScrapingAntResponseConverter.toExtended(raw);

    assertEquals("<html></html>", result.html());
    assertEquals("hello", result.text());
    assertEquals("a=b", result.cookies());
    assertEquals(200, result.statusCode());
    assertEquals("Content-Type", result.headers().getFirst().name());
    assertEquals("text/html", result.headers().getFirst().value());
    assertEquals("https://example.com/api", result.xhrs().getFirst().url());
    assertEquals(201, result.xhrs().getFirst().status());
    assertEquals("POST", result.xhrs().getFirst().method());
    assertEquals("Accept", result.xhrs().getFirst().headers().getFirst().name());
    assertEquals("{}", result.xhrs().getFirst().requestBody());
    assertEquals("{\"ok\":true}", result.xhrs().getFirst().body());
    assertEquals("https://example.com/frame", result.iframes().getFirst().src());
    assertEquals("<p>frame</p>", result.iframes().getFirst().html());
  }

  @Test
  void toExtractMapsNestedValues() {
    String raw =
        """
        {"title":"A","price":12.5,"available":true,"tags":["x","y"],"meta":null}
        """;

    Map<String, Object> result = ScrapingAntResponseConverter.toExtract(raw);

    assertEquals("A", result.get("title"));
    assertEquals(12.5d, (Double) result.get("price"));
    assertEquals(Boolean.TRUE, result.get("available"));
    assertEquals(List.of("x", "y"), result.get("tags"));
    assertTrue(result.containsKey("meta"));
    assertNull(result.get("meta"));
  }

  @Test
  void toExtractSupportsCallerSuppliedType() {
    Type type = new TypeToken<List<String>>() {}.getType();

    List<String> result = ScrapingAntResponseConverter.toExtract("[\"a\",\"b\"]", type);

    assertEquals(List.of("a", "b"), result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void emptyBodyIsReportedWithExtractionType(String raw) {
    assertEmptyResponse(
        ExtractionType.MARKDOWN, () -> ScrapingAntResponseConverter.toMarkdown(raw));
    assertEmptyResponse(ExtractionType.JSON, () -> ScrapingAntResponseConverter.toExtended(raw));
    assertEmptyResponse(
        ExtractionType.AI_EXTRACTED, () -> ScrapingAntResponseConverter.toExtract(raw));
  }

  @Test
  void nullBodyIsReportedWithExtractionType() {
    assertEmptyResponse(
        ExtractionType.MARKDOWN, () -> ScrapingAntResponseConverter.toMarkdown(null));
  }

  @Test
  void jsonNullIsReportedWithExtractionType() {
    ScrapingAntResponseException exception =
        assertThrows(
            ScrapingAntResponseException.class,
            () -> ScrapingAntResponseConverter.toMarkdown("null"));

    assertEquals("JSON null response for MARKDOWN", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("allConverters")
  void malformedJsonIsWrappedWithExtractionType(ExtractionType type, Executable conversion) {
    ScrapingAntResponseException exception =
        assertThrows(ScrapingAntResponseException.class, conversion);

    assertEquals("Failed to deserialize response for " + type, exception.getMessage());
    assertNotNull(exception.getCause());
    assertInstanceOf(JsonSyntaxException.class, exception.getCause());
  }

  @ParameterizedTest
  @ValueSource(strings = {"[]", "\"text\"", "42", "<html>oops</html>"})
  void unexpectedPayloadShapeThrows(String raw) {
    ScrapingAntResponseException exception =
        assertThrows(
            ScrapingAntResponseException.class, () -> ScrapingAntResponseConverter.toMarkdown(raw));

    assertEquals("Failed to deserialize response for MARKDOWN", exception.getMessage());
    assertNotNull(exception.getCause());
  }

  @Test
  void jsonWithMissingFieldsUsesDefaults() {
    MarkdownResponse markdown = ScrapingAntResponseConverter.toMarkdown("{}");
    assertNull(markdown.url());
    assertNull(markdown.markdown());

    ExtendedResponse extended = ScrapingAntResponseConverter.toExtended("{}");
    assertNull(extended.html());
    assertEquals(0, extended.statusCode());
    assertNull(extended.headers());

    assertTrue(ScrapingAntResponseConverter.toExtract("{}").isEmpty());
  }

  private static void assertEmptyResponse(ExtractionType type, Executable conversion) {
    ScrapingAntResponseException exception =
        assertThrows(ScrapingAntResponseException.class, conversion);

    assertEquals("Empty response for " + type, exception.getMessage());
  }

  private static Stream<Arguments> allConverters() {
    return Stream.of(
        Arguments.of(
            ExtractionType.MARKDOWN,
            (Executable) () -> ScrapingAntResponseConverter.toMarkdown("not json")),
        Arguments.of(
            ExtractionType.JSON,
            (Executable) () -> ScrapingAntResponseConverter.toExtended("not json")),
        Arguments.of(
            ExtractionType.AI_EXTRACTED,
            (Executable) () -> ScrapingAntResponseConverter.toExtract("not json")));
  }
}
