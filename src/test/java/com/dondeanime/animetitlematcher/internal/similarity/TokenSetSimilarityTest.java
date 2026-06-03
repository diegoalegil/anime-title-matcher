package com.dondeanime.animetitlematcher.internal.similarity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TokenSetSimilarityTest {

    @Test
    void jaccardOfIdenticalSetsIsOne() {
        assertThat(TokenSetSimilarity.jaccard(Set.of("a", "b"), Set.of("a", "b"))).isEqualTo(1.0);
    }

    @Test
    void jaccardOfDisjointSetsIsZero() {
        assertThat(TokenSetSimilarity.jaccard(Set.of("a", "b"), Set.of("c", "d"))).isEqualTo(0.0);
    }

    @Test
    void jaccardOfPartialOverlap() {
        assertThat(TokenSetSimilarity.jaccard(Set.of("a", "b", "c"), Set.of("b", "c", "d")))
                .isCloseTo(0.5, within(1e-9));
        assertThat(TokenSetSimilarity.jaccard(
                Set.of("my", "hero", "academia"), Set.of("boku", "no", "hero", "academia")))
                .isCloseTo(0.4, within(1e-9));
    }

    @Test
    void jaccardHandlesEmptyAndNull() {
        assertThat(TokenSetSimilarity.jaccard(Set.of(), Set.of())).isEqualTo(1.0);
        assertThat(TokenSetSimilarity.jaccard(null, null)).isEqualTo(1.0);
        assertThat(TokenSetSimilarity.jaccard(Set.of("a"), Set.of())).isEqualTo(0.0);
        assertThat(TokenSetSimilarity.jaccard(null, Set.of("a"))).isEqualTo(0.0);
    }

    @Test
    void sortedJoinIsOrderIndependent() {
        assertThat(TokenSetSimilarity.sortedJoin(List.of("titan", "attack", "on")))
                .isEqualTo(TokenSetSimilarity.sortedJoin(List.of("on", "titan", "attack")));
    }

    @Test
    void tokenSortRatioIgnoresWordOrder() {
        double ratio = TokenSetSimilarity.tokenSortRatio(
                List.of("attack", "on", "titan"), List.of("titan", "attack", "on"), new JaroWinklerSimilarity());
        assertThat(ratio).isEqualTo(1.0);
    }
}
