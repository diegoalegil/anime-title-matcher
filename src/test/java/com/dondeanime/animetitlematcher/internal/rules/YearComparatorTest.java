package com.dondeanime.animetitlematcher.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class YearComparatorTest {

    @Test
    void sameYearScoresOne() {
        assertThat(YearComparator.score(2021, 2021)).hasValue(1.0);
    }

    @Test
    void scoreDegradesWithGap() {
        assertThat(YearComparator.score(2021, 2022).getAsDouble()).isCloseTo(0.85, within(1e-9));
        assertThat(YearComparator.score(2021, 2023).getAsDouble()).isCloseTo(0.60, within(1e-9));
        assertThat(YearComparator.score(2020, 2026).getAsDouble()).isCloseTo(0.05, within(1e-9));
    }

    @Test
    void unknownYearIsAbsent() {
        assertThat(YearComparator.score(null, 2021)).isEmpty();
        assertThat(YearComparator.score(2021, null)).isEmpty();
    }

    @Test
    void largeGapIsHardConflict() {
        assertThat(YearComparator.isHardConflict(2009, 2003)).isTrue();
        assertThat(YearComparator.isHardConflict(2021, 2025)).isTrue();
    }

    @Test
    void smallGapIsNotHardConflict() {
        assertThat(YearComparator.isHardConflict(2021, 2022)).isFalse();
        assertThat(YearComparator.isHardConflict(2021, 2023)).isFalse();
        assertThat(YearComparator.isHardConflict(null, 2021)).isFalse();
    }
}
