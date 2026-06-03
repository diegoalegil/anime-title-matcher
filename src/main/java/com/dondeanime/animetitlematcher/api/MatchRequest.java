package com.dondeanime.animetitlematcher.api;

import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import java.util.List;
import java.util.Objects;

/**
 * A self-contained matching request: the AniList anime, the TMDb candidates to evaluate, and
 * optional per-request {@link ScoringWeights} and {@link MatchThresholds}.
 *
 * <p>When {@code weights} or {@code thresholds} are {@code null} the matcher falls back to the
 * configuration it was created with. The {@code candidates} list is copied defensively and any
 * {@code null} entries are dropped.
 */
public record MatchRequest(
        AniListAnime anime,
        List<TmdbTitle> candidates,
        ScoringWeights weights,
        MatchThresholds thresholds) {

    public MatchRequest {
        candidates = candidates == null
                ? List.of()
                : candidates.stream().filter(Objects::nonNull).toList();
    }

    /** Creates a request that uses the matcher's own weights and thresholds. */
    public static MatchRequest of(AniListAnime anime, List<TmdbTitle> candidates) {
        return new MatchRequest(anime, candidates, null, null);
    }
}
