package com.gapplabs.scrapingant4j.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Resource types that can be blocked during rendering to reduce time and resource usage. */
@Getter
@RequiredArgsConstructor
public enum BlockResource {
  IMAGE("image"),
  STYLESHEET("stylesheet"),
  FONT("font"),
  MEDIA("media"),
  SCRIPT("script"),
  TEXTTRACK("texttrack"),
  XHR("xhr"),
  FETCH("fetch"),
  EVENTSOURCE("eventsource"),
  WEBSOCKET("websocket"),
  MANIFEST("manifest"),
  OTHER("other");

  private final String value;

  @Override
  public String toString() {
    return this.value;
  }
}
