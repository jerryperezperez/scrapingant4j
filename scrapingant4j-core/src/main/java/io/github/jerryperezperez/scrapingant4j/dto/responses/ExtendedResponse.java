package io.github.jerryperezperez.scrapingant4j.dto.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Extended scrape response with rendered content and browser metadata. */
public record ExtendedResponse(
    String html,
    String text,
    String cookies,
    @SerializedName("status_code") int statusCode,
    List<Header> headers,
    List<XhrResponse> xhrs,
    List<IframeResponse> iframes) {

  @Override
  public String toString() {
    return "ExtendedResponse[statusCode="
        + statusCode
        + ", htmlPresent="
        + (html != null)
        + ", textPresent="
        + (text != null)
        + ", headerCount="
        + size(headers)
        + ", xhrCount="
        + size(xhrs)
        + ", iframeCount="
        + size(iframes)
        + "]";
  }

  private static int size(List<?> values) {
    return values == null ? 0 : values.size();
  }

  /** HTTP header name/value pair. */
  public record Header(String name, String value) {}

  /** XMLHttpRequest observed during rendering. */
  public record XhrResponse(
      String url,
      Integer status,
      String method,
      List<Header> headers,
      @SerializedName("request_body") String requestBody,
      String body) {}

  /** Iframe observed during rendering. */
  public record IframeResponse(String src, String html) {}
}
