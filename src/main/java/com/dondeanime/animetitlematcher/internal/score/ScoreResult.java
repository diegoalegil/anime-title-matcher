package com.dondeanime.animetitlematcher.internal.score;

import com.dondeanime.animetitlematcher.api.ScoreBreakdown;
import com.dondeanime.animetitlematcher.domain.TitleVariant;

/**
 * The full outcome of scoring one candidate: the public {@link ScoreBreakdown} plus the flags and
 * matched variants the matcher needs to build its decision and explanation.
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 *
 * @param breakdown              the per-signal breakdown and final score
 * @param exactMatch             whether a title matched exactly after normalisation
 * @param hardConflict           whether a hard format/year conflict capped the score
 * @param matchedAnilistVariant  the AniList variant behind the best title score ({@code null} if none)
 * @param matchedTmdbVariant     the TMDb variant behind the best title score ({@code null} if none)
 */
public record ScoreResult(
        ScoreBreakdown breakdown,
        boolean exactMatch,
        boolean hardConflict,
        TitleVariant matchedAnilistVariant,
        TitleVariant matchedTmdbVariant) {

    public double finalScore() {
        return breakdown.finalScore();
    }
}
