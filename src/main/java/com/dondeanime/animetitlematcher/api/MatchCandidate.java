package com.dondeanime.animetitlematcher.api;

import com.dondeanime.animetitlematcher.domain.TmdbTitle;

/**
 * One TMDb candidate after evaluation: its final {@code score}, the full {@link ScoreBreakdown},
 * the specific titles that matched, and whether a hard conflict was present.
 *
 * @param tmdbTitle          the evaluated candidate
 * @param score              its final score in {@code [0.0, 1.0]}
 * @param breakdown          the per-signal account of the score
 * @param matchedAnilistTitle the AniList title variant behind the best similarity ({@code null} if none)
 * @param matchedTmdbTitle    the TMDb title variant behind the best similarity ({@code null} if none)
 * @param hardConflict       whether a hard format/year conflict capped the score
 */
public record MatchCandidate(
        TmdbTitle tmdbTitle,
        double score,
        ScoreBreakdown breakdown,
        String matchedAnilistTitle,
        String matchedTmdbTitle,
        boolean hardConflict) {
}
