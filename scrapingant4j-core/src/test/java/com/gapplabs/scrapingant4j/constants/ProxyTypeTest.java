package com.gapplabs.scrapingant4j.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProxyTypeTest {

  @ParameterizedTest
  @EnumSource(ProxyType.class)
  void testValueIsNotEmpty(ProxyType proxyType) {
    assertThat(proxyType.getValue()).isNotEmpty();
  }

  @Test
  void testSpecificValues() {
    assertThat(ProxyType.RESIDENTIAL.getValue()).isEqualTo("residential");
    assertThat(ProxyType.DATACENTER.getValue()).isEqualTo("datacenter");
  }

  @Test
  void testEnumValuesCount() {
    assertThat(ProxyType.values()).hasSize(2);
  }
}
