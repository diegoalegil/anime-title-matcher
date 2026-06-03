package com.dondeanime.animetitlematcher.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SequenceComparatorTest {

    private static TitleSequence seq(Integer season, Integer part, boolean finalMarker) {
        return new TitleSequence(season, part, null, finalMarker, FormatHint.NONE, "base");
    }

    @Test
    void absentWhenNeitherSideHasInfo() {
        assertThat(SequenceComparator.score(TitleSequence.none("a"), TitleSequence.none("b"))).isEmpty();
    }

    @Test
    void absentWhenOnlyOneSideHasInfo() {
        // The common case: AniList season 2 vs a TMDb show entry with no season in its title.
        assertThat(SequenceComparator.score(seq(2, null, false), TitleSequence.none("show"))).isEmpty();
        assertThat(SequenceComparator.score(TitleSequence.none("show"), seq(2, null, false))).isEmpty();
    }

    @Test
    void matchingSeasonsScoreHigh() {
        assertThat(SequenceComparator.score(seq(2, null, false), seq(2, null, false))).hasValue(1.0);
    }

    @Test
    void mismatchedSeasonsScoreLow() {
        assertThat(SequenceComparator.score(seq(2, null, false), seq(3, null, false))).hasValue(0.15);
    }

    @Test
    void seasonAndPartBothConsidered() {
        assertThat(SequenceComparator.score(seq(3, 2, false), seq(3, 2, false))).hasValue(1.0);
        assertThat(SequenceComparator.score(seq(3, 2, false), seq(3, 1, false))).hasValue(0.40);
    }

    @Test
    void finalMarkersOnBothSidesMatch() {
        assertThat(SequenceComparator.score(seq(null, null, true), seq(null, null, true))).hasValue(1.0);
    }

    @Test
    void partsWithoutSeasonsCompared() {
        assertThat(SequenceComparator.score(seq(null, 2, false), seq(null, 2, false))).hasValue(1.0);
        assertThat(SequenceComparator.score(seq(null, 2, false), seq(null, 3, false))).hasValue(0.30);
    }
}
