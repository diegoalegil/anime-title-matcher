package com.dondeanime.animetitlematcher.internal.rules;

import java.util.OptionalDouble;

/**
 * Compares episode counts as a soft signal: equal counts score {@code 1.0} and the score falls off
 * with the relative difference. When either count is unknown or non-positive the signal is absent
 * and excluded from the weighted score.
 *
 * <p>Episode counts are intentionally <em>not</em> treated as a hard conflict: AniList counts
 * per-season while TMDb often counts every aired episode, so large but legitimate differences are
 * common. Series-versus-film mismatches are handled by {@link FormatMapper} instead, which avoids
 * penalising the same disagreement twice.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class EpisodeCountComparator {

    private EpisodeCountComparator() {
    }

    public static OptionalDouble score(Integer anilistEpisodes, Integer tmdbEpisodes) {
        if (anilistEpisodes == null || tmdbEpisodes == null || anilistEpisodes <= 0 || tmdbEpisodes <= 0) {
            return OptionalDouble.empty();
        }
        if (anilistEpisodes.equals(tmdbEpisodes)) {
            return OptionalDouble.of(1.0);
        }
        int difference = Math.abs(anilistEpisodes - tmdbEpisodes);
        int larger = Math.max(anilistEpisodes, tmdbEpisodes);
        return OptionalDouble.of(Math.max(0.0, 1.0 - (double) difference / larger));
    }
}
