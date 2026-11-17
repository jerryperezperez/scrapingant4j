package com.gapplabs.constants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyTypeTest {

    @ParameterizedTest
    @EnumSource(ProxyType.class)
    void testValueIsNotEmpty(ProxyType proxyType) {
        assertThat(proxyType.getValue()).isNotEmpty();
    }

    @Test
    void testSpecificValues() {
        assertThat(ProxyType.RESIDENTIAL.getValue()).isEqualTo("residential");
        assertThat(ProxyType.STANDARD.getValue()).isEqualTo("datacenter");
    }

    @Test
    void testEnumValuesCount() {
        assertThat(ProxyType.values()).hasSize(2);
    }
}
