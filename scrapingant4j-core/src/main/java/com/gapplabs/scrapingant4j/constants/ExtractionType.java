package com.gapplabs.scrapingant4j.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Extraction endpoints supported by the ScrapingAnt API. */
@Getter
@RequiredArgsConstructor
public enum ExtractionType {
  GENERAL("general"),
  MARKDOWN("markdown"),
  EXTENDED("extended"),
  AI_EXTRACTED("extract");

  private final String value;
}
