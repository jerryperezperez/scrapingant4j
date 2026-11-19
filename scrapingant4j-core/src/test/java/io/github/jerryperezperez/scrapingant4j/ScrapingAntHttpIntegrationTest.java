package io.github.jerryperezperez.scrapingant4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import io.github.jerryperezperez.scrapingant4j.dto.ScrapingAntRequest;
import io.github.jerryperezperez.scrapingant4j.exceptions.ScrapingAntHttpException;
import io.github.jerryperezperez.scrapingant4j.exceptions.ScrapingAntNetworkException;
import io.github.jerryperezperez.scrapingant4j.exceptions.ScrapingAntResponseException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ScrapingAntHttpIntegrationTest {
  @Test
  void sendsAuthenticatedGetWithEncodedQueryAndMapsResponse() throws IOException {
    AtomicInteger status = new AtomicInteger(200);
    HttpServer server =
        server(
            exchange -> {
              assertEquals("GET", exchange.getRequestMethod());
              assertEquals("/v2/general", exchange.getRequestURI().getPath());
              assertEquals("integration-key", exchange.getRequestHeaders().getFirst("x-api-key"));
              assertNull(exchange.getRequestHeaders().getFirst("Content-Type"));
              Map<String, String> query =
                  Arrays.stream(exchange.getRequestURI().getRawQuery().split("&"))
                      .map(part -> part.split("=", 2))
                      .collect(
                          Collectors.toMap(
                              pair -> decode(pair[0]),
                              pair -> decode(pair.length == 2 ? pair[1] : "")));
              assertEquals("https://example.com/a%20b", query.get("url"));
              assertEquals("YQ==", query.get("js_snippet"));
              assertEquals("#main area", query.get("css_selector"));
              assertEquals("true", query.get("browser"));
              assertFalse(query.containsKey("proxy_type"));
              respond(exchange, status.get(), "<html>ok</html>", Map.of());
            });
    try {
      ScrapingAntClient client = client(server, Duration.ofSeconds(1));
      ScrapingAntRequest request =
          ScrapingAntRequest.builder()
              .url("https://example.com/a%20b")
              .browser(true)
              .jsSnippet("a")
              .cssSelector("#main area")
              .build();
      assertEquals("<html>ok</html>", client.executeGeneral(request));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void mapsHttpErrorsAndRetryAfterThroughRealFeignClient() throws IOException {
    HttpServer server =
        server(
            exchange ->
                respond(exchange, 429, "{\"detail\":\"slow down\"}", Map.of("Retry-After", "120")));

    ScrapingAntClient client = client(server, Duration.ofSeconds(1));
    ScrapingAntRequest request = request();
    try {
      ScrapingAntHttpException error =
          assertThrows(ScrapingAntHttpException.class, () -> client.executeGeneral(request));
      assertEquals(429, error.getStatusCode());
      assertEquals("slow down", error.getMessage());
      assertEquals(Duration.ofSeconds(120), error.getRetryAfter());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void returnsServerErrorWithoutRetrying() throws IOException {
    AtomicInteger attempts = new AtomicInteger();
    HttpServer server =
        server(
            exchange -> {
              attempts.incrementAndGet();
              respond(exchange, 503, "busy", Map.of());
            });

    ScrapingAntClient client = client(server, Duration.ofSeconds(1));
    ScrapingAntRequest request = request();
    try {
      assertEquals(
          503,
          assertThrows(ScrapingAntHttpException.class, () -> client.executeGeneral(request))
              .getStatusCode());
      assertEquals(1, attempts.get());
    } finally {
      server.stop(0);
    }
  }

  @Test
  void appliesReadTimeoutAndConvertsTransportFailure() throws IOException {
    Duration readTimeout = Duration.ofMillis(500);
    CountDownLatch releaseResponse = new CountDownLatch(1);
    HttpServer server =
        server(
            exchange -> {
              try {
                releaseResponse.await();
              } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
              }
              respond(exchange, 200, "ok", Map.of());
            });

    ScrapingAntClient client = client(server, readTimeout);
    ScrapingAntRequest request = request();
    try {
      assertThrows(ScrapingAntNetworkException.class, () -> client.executeGeneral(request));
    } finally {
      releaseResponse.countDown();
      server.stop(0);
    }
  }

  @Test
  void malformedTypedResponseIsReportedClearly() throws IOException {
    HttpServer server = server(exchange -> respond(exchange, 200, "", Map.of()));
    ScrapingAntClient client = client(server, Duration.ofSeconds(1));
    ScrapingAntRequest request = request();

    try {
      assertThrows(ScrapingAntResponseException.class, () -> client.executeMarkdown(request));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void malformedJsonResponseIsReportedClearly() throws IOException {
    HttpServer server = server(exchange -> respond(exchange, 200, "not-json", Map.of()));

    ScrapingAntClient client = client(server, Duration.ofSeconds(1));
    ScrapingAntRequest request = request();

    try {
      assertThrows(ScrapingAntResponseException.class, () -> client.executeMarkdown(request));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void emptyGeneralResponseRemainsAValidBody() throws IOException {
    HttpServer server = server(exchange -> respond(exchange, 200, "", Map.of()));
    try {
      assertEquals("", client(server, Duration.ofSeconds(1)).executeGeneral(request()));
    } finally {
      server.stop(0);
    }
  }

  private static ScrapingAntClient client(HttpServer server, Duration readTimeout) {
    ScrapingAntClientOptions options =
        ScrapingAntClientOptions.builder()
            .apiKey("integration-key")
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(readTimeout)
            .build();
    return new ScrapingAntClient(
        options,
        ScrapingAntClient.buildApi(
            options, "http://localhost:" + server.getAddress().getPort() + "/v2"));
  }

  private static ScrapingAntRequest request() {
    return ScrapingAntRequest.builder().url("https://example.com").build();
  }

  private static HttpServer server(com.sun.net.httpserver.HttpHandler handler) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", handler);
    server.start();
    return server;
  }

  private static void respond(
      com.sun.net.httpserver.HttpExchange exchange,
      int status,
      String body,
      Map<String, String> headers)
      throws IOException {
    headers.forEach((name, value) -> exchange.getResponseHeaders().set(name, value));
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }

  private static String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }
}
