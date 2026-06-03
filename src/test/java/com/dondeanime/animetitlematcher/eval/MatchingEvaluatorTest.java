package com.dondeanime.animetitlematcher.eval;

import static org.assertj.core.api.Assertions.assertThat;

import com.dondeanime.animetitlematcher.api.AnimeTitleMatcher;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchingEvaluatorTest {

    @Test
    void bundledSampleIsMatchedAccurately() {
        List<EvaluationCase> cases = FixtureLoader.loadCases("/fixtures/matching-cases.json");
        assertThat(cases).isNotEmpty();

        MatchingEvaluator.Metrics metrics =
                new MatchingEvaluator(AnimeTitleMatcher.createDefault()).evaluate(cases);

        assertThat(metrics.total()).isEqualTo(cases.size());
        assertThat(metrics.correct() + metrics.incorrect() + metrics.noMatch() + metrics.ambiguous())
                .isEqualTo(metrics.total());
        // The bundled sample is small and curated, so the matcher resolves all of it today.
        // A 0.90 floor guards against regressions without being brittle to future fixtures.
        assertThat(metrics.accuracy())
                .withFailMessage("accuracy was %s (metrics=%s)", metrics.accuracy(), metrics)
                .isGreaterThanOrEqualTo(0.90);
    }

    @Test
    void standaloneSampleFilesParse() {
        assertThat(FixtureLoader.loadAnilistSample("/fixtures/anilist-sample.json")).isNotEmpty();
        assertThat(FixtureLoader.loadTmdbSample("/fixtures/tmdb-candidates-sample.json")).hasSize(3);
    }
}
