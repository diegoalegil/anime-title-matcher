package com.dondeanime.animetitlematcher.internal.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RomanNumeralConverterTest {

    @ParameterizedTest
    @CsvSource({
            "ii, 2",
            "iii, 3",
            "iv, 4",
            "ix, 9",
            "x, 10",
            "xiii, 13",
            "xiv, 14",
            "v, 5",
            "xx, 20",
    })
    void convertsValidNumerals(String token, String expected) {
        assertThat(RomanNumeralConverter.convertToken(token)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"i", "l", "c", "d", "m"})
    void leavesAmbiguousSingleLettersUntouched(String token) {
        assertThat(RomanNumeralConverter.convertToken(token)).isEqualTo(token);
    }

    @ParameterizedTest
    @ValueSource(strings = {"iiii", "vv", "ic", "naruto", "season"})
    void leavesNonNumeralsUntouched(String token) {
        assertThat(RomanNumeralConverter.convertToken(token)).isEqualTo(token);
    }

    @Test
    void isCaseInsensitive() {
        assertThat(RomanNumeralConverter.convertToken("IV")).isEqualTo("4");
        assertThat(RomanNumeralConverter.convertToken("XIII")).isEqualTo("13");
    }

    @Test
    void returnsNullForNull() {
        assertThat(RomanNumeralConverter.convertToken(null)).isNull();
    }

    @Test
    void validatesWellFormedNumerals() {
        assertThat(RomanNumeralConverter.isRomanNumeral("xiv")).isTrue();
        assertThat(RomanNumeralConverter.isRomanNumeral("mcmxc")).isTrue();
        assertThat(RomanNumeralConverter.isRomanNumeral("iiii")).isFalse();
        assertThat(RomanNumeralConverter.isRomanNumeral("abc")).isFalse();
        assertThat(RomanNumeralConverter.isRomanNumeral("")).isFalse();
        assertThat(RomanNumeralConverter.isRomanNumeral(null)).isFalse();
    }
}
