package com.dondeanime.animetitlematcher.api;

import java.util.List;

/**
 * A human-readable account of why the matcher reached its decision, plus the structured pieces
 * behind it: the titles that matched, the signals that contributed, and a rejection reason when no
 * reliable match was found.
 *
 * @param summary            a one-line, human-readable explanation
 * @param matchedAnilistTitle the AniList title that matched ({@code null} if none)
 * @param matchedTmdbTitle    the TMDb title that matched ({@code null} if none)
 * @param signalsUsed        the signals that were present and contributed (e.g. {@code title}, {@code year})
 * @param rejectionReason    why the best candidate was rejected or flagged ({@code null} if confidently matched)
 */
public record MatchExplanation(
        String summary,
        String matchedAnilistTitle,
        String matchedTmdbTitle,
        List<String> signalsUsed,
        String rejectionReason) {

    public MatchExplanation {
        signalsUsed = signalsUsed == null ? List.of() : List.copyOf(signalsUsed);
    }
}
