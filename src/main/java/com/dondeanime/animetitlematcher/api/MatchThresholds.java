package com.dondeanime.animetitlematcher.api;

/**
 * Turns a numeric final score into a {@link MatchDecision} and supplies the tuning constants used
 * by the matcher.
 *
 * <p>Hard conflicts are enforced as score <em>caps</em> ({@code finalScore = min(raw, cap)}) rather
 * than additive penalties, so a conflict is never counted twice. {@link #ambiguityDelta} is the
 * minimum score gap the best candidate must hold over the runner-up to avoid being reported as
 * {@link MatchDecision#AMBIGUOUS}.
 *
 * @param exactMatch         minimum score for {@link MatchDecision#EXACT_MATCH}
 * @param highConfidence     minimum score for {@link MatchDecision#HIGH_CONFIDENCE}
 * @param mediumConfidence   minimum score for {@link MatchDecision#MEDIUM_CONFIDENCE}
 * @param lowConfidence      minimum score for {@link MatchDecision#LOW_CONFIDENCE}
 * @param ambiguityDelta     minimum gap over the runner-up to avoid an ambiguous verdict
 * @param formatConflictCap  score ceiling when a TV-vs-movie conflict is present
 * @param yearConflictCap    score ceiling when a large year gap is present
 */
public record MatchThresholds(
        double exactMatch,
        double highConfidence,
        double mediumConfidence,
        double lowConfidence,
        double ambiguityDelta,
        double formatConflictCap,
        double yearConflictCap) {

    /** Sensible defaults tuned for AniList/TMDb matching. */
    public static MatchThresholds defaults() {
        return new MatchThresholds(0.95, 0.85, 0.70, 0.55, 0.05, 0.45, 0.70);
    }

    /**
     * Maps a final score to a base decision. {@link MatchDecision#AMBIGUOUS} is decided separately
     * by the matcher, which alone knows the runner-up's score.
     *
     * @param finalScore   the candidate's final score
     * @param exact        whether the titles were identical after normalisation
     * @param hardConflict whether a hard format/year conflict is present
     */
    public MatchDecision decide(double finalScore, boolean exact, boolean hardConflict) {
        if (exact && !hardConflict && finalScore >= exactMatch) {
            return MatchDecision.EXACT_MATCH;
        }
        if (finalScore >= highConfidence) {
            return MatchDecision.HIGH_CONFIDENCE;
        }
        if (finalScore >= mediumConfidence) {
            return MatchDecision.MEDIUM_CONFIDENCE;
        }
        if (finalScore >= lowConfidence) {
            return MatchDecision.LOW_CONFIDENCE;
        }
        return MatchDecision.NO_MATCH;
    }
}
