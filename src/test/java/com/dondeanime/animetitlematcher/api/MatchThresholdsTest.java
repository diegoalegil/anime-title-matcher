package com.dondeanime.animetitlematcher.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MatchThresholdsTest {

    private final MatchThresholds thresholds = MatchThresholds.defaults();

    @Test
    void exactRequiresExactFlagHighScoreAndNoHardConflict() {
        assertThat(thresholds.decide(0.97, true, false)).isEqualTo(MatchDecision.EXACT_MATCH);
        assertThat(thresholds.decide(0.97, false, false)).isEqualTo(MatchDecision.HIGH_CONFIDENCE);
        assertThat(thresholds.decide(0.97, true, true)).isEqualTo(MatchDecision.HIGH_CONFIDENCE);
    }

    @Test
    void mapsScoreBandsToDecisions() {
        assertThat(thresholds.decide(0.88, false, false)).isEqualTo(MatchDecision.HIGH_CONFIDENCE);
        assertThat(thresholds.decide(0.75, false, false)).isEqualTo(MatchDecision.MEDIUM_CONFIDENCE);
        assertThat(thresholds.decide(0.60, false, false)).isEqualTo(MatchDecision.LOW_CONFIDENCE);
        assertThat(thresholds.decide(0.40, false, false)).isEqualTo(MatchDecision.NO_MATCH);
    }

    @Test
    void neverReturnsAmbiguousFromDecide() {
        for (double score = 0.0; score <= 1.0; score += 0.05) {
            assertThat(thresholds.decide(score, false, false)).isNotEqualTo(MatchDecision.AMBIGUOUS);
        }
    }

    @Test
    void weightsRejectNegativeValues() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new ScoringWeights(-1, 0, 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
