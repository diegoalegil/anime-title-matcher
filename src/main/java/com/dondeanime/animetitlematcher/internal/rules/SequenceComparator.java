package com.dondeanime.animetitlematcher.internal.rules;

import java.util.OptionalDouble;

/**
 * Compares the season/part/cour metadata of an AniList anime and a TMDb candidate.
 *
 * <p>The signal is only produced when <em>both</em> sides carry sequence information, because the
 * common arrangement is one TMDb entry per show (no season in its title) versus one AniList entry
 * per season. In that asymmetric case the signal is absent so a correct show/season match is not
 * penalised; the season markers have already been stripped from the titles for similarity. When
 * both sides do carry explicit seasons, agreement is rewarded and disagreement scores low.
 *
 * <p>This is a soft signal (no hard conflict), so a season disagreement nudges ranking without
 * vetoing a candidate outright.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class SequenceComparator {

    private SequenceComparator() {
    }

    public static OptionalDouble score(TitleSequence anilist, TitleSequence tmdb) {
        if (anilist == null || tmdb == null || !anilist.hasSequenceInfo() || !tmdb.hasSequenceInfo()) {
            return OptionalDouble.empty();
        }

        if (anilist.season() != null && tmdb.season() != null) {
            if (!anilist.season().equals(tmdb.season())) {
                return OptionalDouble.of(0.15);
            }
            if (anilist.part() != null && tmdb.part() != null) {
                return OptionalDouble.of(anilist.part().equals(tmdb.part()) ? 1.0 : 0.40);
            }
            return OptionalDouble.of(1.0);
        }

        if (anilist.finalMarker() && tmdb.finalMarker()) {
            return OptionalDouble.of(1.0);
        }

        if (anilist.part() != null && tmdb.part() != null) {
            return OptionalDouble.of(anilist.part().equals(tmdb.part()) ? 1.0 : 0.30);
        }

        return OptionalDouble.of(0.70);
    }
}
