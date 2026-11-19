package io.github.jerryperezperez.scrapingant4j.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Countries available for proxy geolocation in ScrapingAnt. */
@Getter
@RequiredArgsConstructor
public enum ProxyCountry {
  BRAZIL("br"),
  CANADA("ca"),
  CHINA("cn"),
  CZECH_REPUBLIC("cz"),
  FRANCE("fr"),
  GERMANY("de"),
  HONG_KONG("hk"),
  INDIA("in"),
  INDONESIA("id"),
  ITALY("it"),
  ISRAEL("il"),
  JAPAN("jp"),
  NETHERLANDS("nl"),
  POLAND("pl"),
  RUSSIA("ru"),
  SAUDI_ARABIA("sa"),
  SINGAPORE("sg"),
  SOUTH_KOREA("kr"),
  SPAIN("es"),
  UNITED_KINGDOM("gb"),
  UNITED_ARAB_EMIRATES("ae"),
  USA("us"),
  VIETNAM("vn"),
  ALL_COUNTRIES("all");

  private final String value;
}
