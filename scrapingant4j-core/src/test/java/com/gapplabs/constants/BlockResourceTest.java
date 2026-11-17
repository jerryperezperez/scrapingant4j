package com.gapplabs.constants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class BlockResourceTest {

    @ParameterizedTest
    @EnumSource(BlockResource.class)
    void testValueIsNotEmpty(BlockResource blockResource) {
        assertThat(blockResource.getValue()).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(BlockResource.class)
    void testToStringMatchesValue(BlockResource blockResource) {
        assertThat(blockResource.toString()).isEqualTo(blockResource.getValue());
    }

    @Test
    void testSpecificValues() {
        assertThat(BlockResource.IMAGE.getValue()).isEqualTo("image");
        assertThat(BlockResource.STYLESHEET.getValue()).isEqualTo("stylesheet");
        assertThat(BlockResource.FONT.getValue()).isEqualTo("font");
        assertThat(BlockResource.SCRIPT.getValue()).isEqualTo("script");
    }

    @Test
    void testEnumValuesCount() {
        assertThat(BlockResource.values()).hasSize(12);
    }
}
