package com.gapplabs.scrapingant4j.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProxyType {
  RESIDENTIAL("residential"),
  STANDARD("datacenter");

  private final String value;
}
