package io.github.jerryperezperez.scrapingant4j.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Proxy types supported by the ScrapingAnt API. */
@Getter
@RequiredArgsConstructor
public enum ProxyType {
  RESIDENTIAL("residential"),
  STANDARD("datacenter");

  private final String value;
}
