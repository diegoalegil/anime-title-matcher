package com.dondeanime.animetitlematcher.eval;

import com.dondeanime.animetitlematcher.api.AnimeTitleMatcher;
import com.dondeanime.animetitlematcher.api.MatchDecision;
import com.dondeanime.animetitlematcher.api.MatchResult;
import java.util.List;

/**
 * Runs the matcher over a list of {@link EvaluationCase}s and reports aggregate accuracy metrics.
 * This is a simple, offline way to measure the matcher against a local dataset; it never touches
 * the network.
 */
public final class MatchingEvaluator {

    private final AnimeTitleMatcher matcher;

    public MatchingEvaluator(AnimeTitleMatcher matcher) {
        this.matcher = matcher;
    }

    public Metrics evaluate(List<EvaluationCase> cases) {
        int correct = 0;
        int incorrect = 0;
        int noMatch = 0;
        int ambiguous = 0;

        for (EvaluationCase evaluationCase : cases) {
            MatchResult result = matcher.findBestMatch(evaluationCase.anime(), evaluationCase.candidates());
            MatchDecision decision = result.decision();

            if (evaluationCase.expectMatch()) {
                if (decision == MatchDecision.AMBIGUOUS) {
                    ambiguous++;
                } else if (decision == MatchDecision.NO_MATCH) {
                    noMatch++;
                } else if (isExpectedCandidate(result, evaluationCase.expectedTmdbId())) {
                    correct++;
                } else {
                    incorrect++;
                }
            } else if (decision == MatchDecision.NO_MATCH) {
                correct++;
            } else {
                incorrect++;
            }
        }

        double accuracy = cases.isEmpty() ? 0.0 : (double) correct / cases.size();
        return new Metrics(cases.size(), correct, incorrect, noMatch, ambiguous, accuracy);
    }

    private static boolean isExpectedCandidate(MatchResult result, Long expectedTmdbId) {
        return expectedTmdbId != null
                && result.bestCandidate() != null
                && result.bestCandidate().tmdbTitle().id() == expectedTmdbId;
    }

    /**
     * Aggregate metrics over an evaluation run.
     *
     * @param total     number of cases evaluated
     * @param correct   cases resolved exactly as expected
     * @param incorrect cases matched to the wrong candidate (or a false positive on a no-match case)
     * @param noMatch   cases that expected a match but produced NO_MATCH
     * @param ambiguous cases that expected a match but were flagged ambiguous
     * @param accuracy  {@code correct / total}
     */
    public record Metrics(int total, int correct, int incorrect, int noMatch, int ambiguous, double accuracy) {
    }
}
