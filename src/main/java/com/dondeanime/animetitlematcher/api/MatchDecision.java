package com.dondeanime.animetitlematcher.api;

/**
 * The outcome of a match attempt, ordered from strongest to weakest confidence.
 *
 * <ul>
 *   <li>{@link #EXACT_MATCH} – titles are identical after normalisation and there is no hard
 *       conflict in format, year or episodes.</li>
 *   <li>{@link #HIGH_CONFIDENCE} / {@link #MEDIUM_CONFIDENCE} / {@link #LOW_CONFIDENCE} – the score
 *       cleared the corresponding threshold.</li>
 *   <li>{@link #AMBIGUOUS} – the best candidate is too close to the runner-up to be trusted.</li>
 *   <li>{@link #NO_MATCH} – no candidate cleared the lowest threshold.</li>
 * </ul>
 */
public enum MatchDecision {

    EXACT_MATCH,
    HIGH_CONFIDENCE,
    MEDIUM_CONFIDENCE,
    LOW_CONFIDENCE,
    AMBIGUOUS,
    NO_MATCH;

    /** Whether this decision points at a usable candidate (anything other than {@link #NO_MATCH}). */
    public boolean isMatch() {
        return this != NO_MATCH;
    }

    /** Whether this is a strong, unambiguous match. */
    public boolean isConfident() {
        return this == EXACT_MATCH || this == HIGH_CONFIDENCE;
    }
}
