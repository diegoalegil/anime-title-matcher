package com.dondeanime.animetitlematcher.internal.similarity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CompositeTitleSimilarityTest {

    private final CompositeTitleSimilarity similarity = new CompositeTitleSimilarity();

    @Test
    void identicalTitlesScoreOne() {
        assertThat(similarity.similarity("naruto", "naruto")).isEqualTo(1.0);
        assertThat(similarity.similarity("fate stay night", "fate stay night")).isEqualTo(1.0);
    }

    @Test
    void emptyHandling() {
        assertThat(similarity.similarity("", "")).isEqualTo(1.0);
        assertThat(similarity.similarity("naruto", "")).isEqualTo(0.0);
        assertThat(similarity.similarity("", "naruto")).isEqualTo(0.0);
    }

    @Test
    void nullNormalizedTitlesScoreZero() {
        assertThat(similarity.similarity((com.dondeanime.animetitlematcher.internal.normalize.NormalizedTitle) null,
                (com.dondeanime.animetitlematcher.internal.normalize.NormalizedTitle) null)).isEqualTo(0.0);
    }

    @Test
    void similarButDistinctTitlesScoreBelowOne() {
        double score = similarity.similarity("naruto", "boruto");
        assertThat(score).isGreaterThan(0.4).isLessThan(1.0);
    }

    @Test
    void wordReorderingStaysHigh() {
        assertThat(similarity.similarity("attack on titan", "titan attack on")).isGreaterThan(0.95);
    }

    @Test
    void sharedTokensAcrossTranslationsScoreModerate() {
        double score = similarity.similarity("my hero academia", "boku no hero academia");
        assertThat(score).isGreaterThan(0.5).isLessThan(0.9);
    }

    @Test
    void unrelatedTranslatedTitlesScoreLow() {
        double score = similarity.similarity("attack on titan", "shingeki no kyojin");
        assertThat(score).isLessThan(0.45);
    }

    @Test
    void translationOverlapBeatsUnrelatedPair() {
        double overlap = similarity.similarity("my hero academia", "boku no hero academia");
        double unrelated = similarity.similarity("attack on titan", "shingeki no kyojin");
        assertThat(overlap).isGreaterThan(unrelated);
    }

    @Test
    void isSymmetricAndWithinRange() {
        String[] samples = {"naruto", "boruto", "fate stay night", "shingeki no kyojin", "attack on titan"};
        for (String left : samples) {
            for (String right : samples) {
                double leftRight = similarity.similarity(left, right);
                double rightLeft = similarity.similarity(right, left);
                assertThat(leftRight).isBetween(0.0, 1.0);
                assertThat(leftRight).isEqualTo(rightLeft);
            }
        }
    }
}
