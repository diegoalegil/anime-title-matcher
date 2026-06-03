package com.dondeanime.animetitlematcher.internal.similarity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class JaroWinklerSimilarityTest {

    private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    @Test
    void identicalStringsScoreOne() {
        assertThat(similarity.similarity("naruto", "naruto")).isEqualTo(1.0);
    }

    @Test
    void twoEmptyStringsScoreOne() {
        assertThat(similarity.similarity("", "")).isEqualTo(1.0);
        assertThat(similarity.similarity(null, null)).isEqualTo(1.0);
    }

    @Test
    void oneEmptyStringScoresZero() {
        assertThat(similarity.similarity("naruto", "")).isEqualTo(0.0);
    }

    @Test
    void matchesKnownReferenceValues() {
        assertThat(JaroWinklerSimilarity.jaroWinkler("martha", "marhta")).isCloseTo(0.961, within(0.005));
        assertThat(JaroWinklerSimilarity.jaroWinkler("dwayne", "duane")).isCloseTo(0.840, within(0.005));
    }

    @Test
    void prefixBonusRaisesScoreAbovePlainJaro() {
        double jaro = JaroWinklerSimilarity.jaro("frieren", "frieden");
        double jaroWinkler = JaroWinklerSimilarity.jaroWinkler("frieren", "frieden");
        assertThat(jaroWinkler).isGreaterThanOrEqualTo(jaro);
    }

    @Test
    void isSymmetric() {
        assertThat(similarity.similarity("steins gate", "stein gate"))
                .isCloseTo(similarity.similarity("stein gate", "steins gate"), within(1e-12));
    }

    @Test
    void alwaysWithinUnitRange() {
        String[] samples = {"", "a", "naruto", "boruto", "fullmetal alchemist brotherhood"};
        for (String left : samples) {
            for (String right : samples) {
                assertThat(similarity.similarity(left, right)).isBetween(0.0, 1.0);
            }
        }
    }
}
