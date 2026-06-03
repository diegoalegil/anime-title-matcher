package com.dondeanime.animetitlematcher.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class EpisodeCountComparatorTest {

    @Test
    void equalCountsScoreOne() {
        assertThat(EpisodeCountComparator.score(12, 12)).hasValue(1.0);
    }

    @Test
    void scoreFallsOffWithRelativeDifference() {
        assertThat(EpisodeCountComparator.score(12, 13).getAsDouble()).isCloseTo(1.0 - 1.0 / 13.0, within(1e-9));
        assertThat(EpisodeCountComparator.score(12, 24).getAsDouble()).isCloseTo(0.5, within(1e-9));
        assertThat(EpisodeCountComparator.score(12, 1).getAsDouble()).isCloseTo(1.0 - 11.0 / 12.0, within(1e-9));
    }

    @Test
    void unknownOrNonPositiveIsAbsent() {
        assertThat(EpisodeCountComparator.score(null, 12)).isEmpty();
        assertThat(EpisodeCountComparator.score(12, null)).isEmpty();
        assertThat(EpisodeCountComparator.score(0, 12)).isEmpty();
        assertThat(EpisodeCountComparator.score(12, -3)).isEmpty();
    }

    @Test
    void scoreStaysWithinRange() {
        assertThat(EpisodeCountComparator.score(1, 1000).getAsDouble()).isBetween(0.0, 1.0);
    }
}
