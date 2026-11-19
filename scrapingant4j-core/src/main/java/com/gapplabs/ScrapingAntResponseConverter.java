package com.gapplabs;

import com.gapplabs.constants.ExtractionType;
import com.gapplabs.dto.responses.ExtendedResponse;
import com.gapplabs.dto.responses.MarkdownResponse;
import com.gapplabs.exceptions.ScrapingAntResponseException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

/** Converts raw ScrapingAnt API responses into typed objects. */
final class ScrapingAntResponseConverter {

  private static final Gson GSON = new Gson();
  private static final Type STRING_OBJECT_MAP = new TypeToken<Map<String, Object>>() {}.getType();

  private ScrapingAntResponseConverter() {}

  static MarkdownResponse toMarkdown(String raw) {
    return deserialize(raw, MarkdownResponse.class, ExtractionType.MARKDOWN);
  }

  static ExtendedResponse toExtended(String raw) {
    return deserialize(raw, ExtendedResponse.class, ExtractionType.JSON);
  }

  static Map<String, Object> toExtract(String raw) {
    return deserialize(raw, STRING_OBJECT_MAP, ExtractionType.AI_EXTRACTED);
  }

  static <T> T toExtract(String raw, Type type) {
    return deserialize(raw, type, ExtractionType.AI_EXTRACTED);
  }

  private static <T> T deserialize(String raw, Type type, ExtractionType extractionType) {
    if (raw == null || raw.isBlank()) {
      throw new ScrapingAntResponseException("Empty response for " + extractionType);
    }
    try {
      T result = GSON.fromJson(raw, type);
      if (result == null) {
        throw new ScrapingAntResponseException("JSON null response for " + extractionType);
      }
      return result;
    } catch (ScrapingAntResponseException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw new ScrapingAntResponseException(
          "Failed to deserialize response for " + extractionType, exception);
    }
  }
}
