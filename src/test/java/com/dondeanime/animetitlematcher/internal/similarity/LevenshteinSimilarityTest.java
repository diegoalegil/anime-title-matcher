package com.dondeanime.animetitlematcher.internal.similarity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class LevenshteinSimilarityTest {

    private final LevenshteinSimilarity similarity = new LevenshteinSimilarity();

    @Test
    void distanceOfClassicExamples() {
        assertThat(LevenshteinSimilarity.distance("kitten", "sitting")).isEqualTo(3);
        assertThat(LevenshteinSimilarity.distance("flaw", "lawn")).isEqualTo(2);
    }

    @Test
    void distanceHandlesEmptyAndNull() {
        assertThat(LevenshteinSimilarity.distance("", "")).isZero();
        assertThat(LevenshteinSimilarity.distance("abc", "")).isEqualTo(3);
        assertThat(LevenshteinSimilarity.distance("", "abc")).isEqualTo(3);
        assertThat(LevenshteinSimilarity.distance(null, "abc")).isEqualTo(3);
        assertThat(LevenshteinSimilarity.distance("abc", null)).isEqualTo(3);
    }

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
    void similarButDifferentScoresBetweenZeroAndOne() {
        double score = similarity.similarity("naruto", "boruto");
        assertThat(score).isGreaterThan(0.0).isLessThan(1.0);
        assertThat(score).isCloseTo(1.0 - 2.0 / 6.0, within(1e-9));
    }

    @Test
    void isSymmetric() {
        assertThat(similarity.similarity("attack on titan", "shingeki no kyojin"))
                .isEqualTo(similarity.similarity("shingeki no kyojin", "attack on titan"));
    }

    @Test
    void alwaysWithinUnitRange() {
        String[] samples = {"", "a", "naruto", "fullmetal alchemist", "進撃の巨人"};
        for (String left : samples) {
            for (String right : samples) {
                double score = similarity.similarity(left, right);
                assertThat(score).isBetween(0.0, 1.0);
            }
        }
    }
}
