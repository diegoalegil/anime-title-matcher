package com.dondeanime.animetitlematcher.eval;

import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import java.util.List;

/**
 * One labelled matching scenario: an AniList anime, the TMDb candidates to choose from, and the
 * expected outcome ({@code expectedTmdbId} when a match is expected, or {@code expectMatch == false}
 * when no candidate is correct).
 */
public record EvaluationCase(
        String name,
        AniListAnime anime,
        List<TmdbTitle> candidates,
        Long expectedTmdbId,
        boolean expectMatch) {
}
