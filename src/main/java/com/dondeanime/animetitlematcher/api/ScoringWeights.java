package com.dondeanime.animetitlematcher.api;

/**
 * Relative importance of each scoring signal. Weights need not sum to {@code 1.0}: the scorer
 * renormalises them over whichever signals are actually present for a given candidate, so a missing
 * year or episode count never drags the score down.
 *
 * @param title    weight of title similarity (the dominant signal)
 * @param season   weight of season/part agreement
 * @param year     weight of release-year proximity
 * @param format   weight of format agreement
 * @param episodes weight of episode-count proximity
 */
public record ScoringWeights(double title, double season, double year, double format, double episodes) {

    public ScoringWeights {
        requireNonNegative(title, "title");
        requireNonNegative(season, "season");
        requireNonNegative(year, "year");
        requireNonNegative(format, "format");
        requireNonNegative(episodes, "episodes");
    }

    /** Balanced defaults with title similarity as the dominant signal. */
    public static ScoringWeights defaults() {
        return new ScoringWeights(0.55, 0.10, 0.13, 0.12, 0.10);
    }

    private static void requireNonNegative(double value, String name) {
        if (value < 0 || Double.isNaN(value)) {
            throw new IllegalArgumentException(name + " weight must be a non-negative number");
        }
    }
}
