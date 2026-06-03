package com.dondeanime.animetitlematcher.api;

import java.util.List;
import java.util.Optional;

/**
 * The outcome of matching one AniList anime against a list of TMDb candidates.
 *
 * <p>{@link #bestCandidate()} is the highest-scoring candidate even when {@link #decision()} is
 * {@link MatchDecision#NO_MATCH} (so callers can see what was closest), and is {@code null} only
 * when no candidates were supplied. {@link #allCandidates()} is sorted by score, descending.
 *
 * @param decision      the overall decision (including ambiguity)
 * @param bestCandidate the top-scoring candidate, or {@code null} if there were none
 * @param allCandidates every candidate, scored and sorted by score descending
 * @param warnings      non-fatal notes (e.g. an ambiguity warning)
 * @param explanation   why this decision was reached
 */
public record MatchResult(
        MatchDecision decision,
        MatchCandidate bestCandidate,
        List<MatchCandidate> allCandidates,
        List<String> warnings,
        MatchExplanation explanation) {

    public MatchResult {
        allCandidates = allCandidates == null ? List.of() : List.copyOf(allCandidates);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    /** Whether the decision points at a usable candidate. */
    public boolean isMatched() {
        return decision.isMatch();
    }

    /** The best candidate as an {@link Optional}. */
    public Optional<MatchCandidate> bestMatch() {
        return Optional.ofNullable(bestCandidate);
    }

    /** The best candidate's score, or {@code 0.0} if there were no candidates. */
    public double score() {
        return bestCandidate == null ? 0.0 : bestCandidate.score();
    }

    /** The best candidate's breakdown, or {@code null} if there were no candidates. */
    public ScoreBreakdown scoreBreakdown() {
        return bestCandidate == null ? null : bestCandidate.breakdown();
    }
}
