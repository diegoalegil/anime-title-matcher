package com.dondeanime.animetitlematcher.domain;

import java.util.Locale;

/**
 * Release format of an anime as reported by AniList, reduced to the cases relevant for
 * matching against TMDb.
 *
 * <p>{@link #UNKNOWN} is used when the source data does not specify a format; the scorer
 * treats it as a neutral signal rather than a conflict.
 */
public enum AnimeFormat {

    TV,
    MOVIE,
    OVA,
    ONA,
    SPECIAL,
    MUSIC,
    UNKNOWN;

    /**
     * Parses an AniList format string (e.g. {@code "TV"}, {@code "TV_SHORT"}, {@code "MOVIE"}).
     * Unrecognised or {@code null} input maps to {@link #UNKNOWN}.
     */
    public static AnimeFormat parse(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "TV", "TV_SHORT" -> TV;
            case "MOVIE" -> MOVIE;
            case "OVA" -> OVA;
            case "ONA" -> ONA;
            case "SPECIAL" -> SPECIAL;
            case "MUSIC" -> MUSIC;
            default -> UNKNOWN;
        };
    }
}
