package com.dondeanime.animetitlematcher.api;

/**
 * A transparent, per-signal account of how a candidate's {@link #finalScore()} was reached.
 *
 * <p>Each signal is in {@code [0.0, 1.0]}. Signals that were not applicable (for example a year
 * comparison when one year is unknown) are {@code null} rather than zero, so callers can tell
 * "absent" apart from "scored zero". {@link #penaltyScore()} is the amount the hard-conflict caps
 * subtracted from the raw weighted average; it is reported for transparency and is not an extra
 * penalty layered on top of the signals.
 *
 * @param titleScore            best similarity among the canonical (primary) titles
 * @param alternativeTitleScore best similarity involving a synonym or alternative title
 * @param seasonScore           season/part agreement, or {@code null} if not comparable
 * @param yearScore             release-year proximity, or {@code null} if unknown
 * @param formatScore           format agreement, or {@code null} if unknown
 * @param episodeScore          episode-count proximity, or {@code null} if unknown
 * @param penaltyScore          reduction applied by hard-conflict caps
 * @param finalScore            the final score in {@code [0.0, 1.0]}
 */
public record ScoreBreakdown(
        double titleScore,
        double alternativeTitleScore,
        Double seasonScore,
        Double yearScore,
        Double formatScore,
        Double episodeScore,
        double penaltyScore,
        double finalScore) {
}
