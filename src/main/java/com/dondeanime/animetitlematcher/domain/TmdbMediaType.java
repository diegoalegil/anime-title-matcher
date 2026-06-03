package com.dondeanime.animetitlematcher.domain;

import java.util.Locale;

/**
 * Media type of a TMDb entry. TMDb distinguishes only {@code tv} and {@code movie};
 * {@link #UNKNOWN} covers missing or unrecognised values.
 */
public enum TmdbMediaType {

    TV,
    MOVIE,
    UNKNOWN;

    /**
     * Maps a raw TMDb media-type string ({@code "tv"} / {@code "movie"}) to this enum.
     * Unrecognised or {@code null} input maps to {@link #UNKNOWN}.
     */
    public static TmdbMediaType fromTmdbString(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "tv" -> TV;
            case "movie" -> MOVIE;
            default -> UNKNOWN;
        };
    }
}
