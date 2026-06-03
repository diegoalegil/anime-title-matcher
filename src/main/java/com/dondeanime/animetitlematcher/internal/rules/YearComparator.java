package com.dondeanime.animetitlematcher.internal.rules;

import java.util.OptionalDouble;

/**
 * Compares the release years of an AniList anime and a TMDb candidate.
 *
 * <p>The score degrades with the gap (same year is best, ±1 still strong, then falling off). When
 * either year is unknown the signal is absent ({@link OptionalDouble#empty()}) and is excluded from
 * the weighted score rather than penalising the candidate. A gap of {@value #HARD_CONFLICT_GAP}
 * years or more is treated as a hard conflict, which caps the final score so a wrong-year match
 * cannot be reported as high confidence.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class YearComparator {

    static final int HARD_CONFLICT_GAP = 4;

    private YearComparator() {
    }

    public static OptionalDouble score(Integer anilistYear, Integer tmdbYear) {
        if (anilistYear == null || tmdbYear == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(scoreForGap(Math.abs(anilistYear - tmdbYear)));
    }

    public static boolean isHardConflict(Integer anilistYear, Integer tmdbYear) {
        if (anilistYear == null || tmdbYear == null) {
            return false;
        }
        return Math.abs(anilistYear - tmdbYear) >= HARD_CONFLICT_GAP;
    }

    private static double scoreForGap(int gap) {
        if (gap == 0) {
            return 1.0;
        }
        if (gap == 1) {
            return 0.85;
        }
        if (gap == 2) {
            return 0.60;
        }
        if (gap == 3) {
            return 0.35;
        }
        if (gap <= 5) {
            return 0.15;
        }
        return 0.05;
    }
}
