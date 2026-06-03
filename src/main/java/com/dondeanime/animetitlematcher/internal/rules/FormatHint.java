package com.dondeanime.animetitlematcher.internal.rules;

/**
 * Format clue extracted from the text of a title (e.g. a trailing {@code "Movie"} or {@code "OVA"}),
 * as opposed to the structured format reported by AniList. Used as a secondary signal when the
 * structured metadata is missing or ambiguous.
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 */
public enum FormatHint {
    NONE,
    MOVIE,
    OVA,
    ONA,
    SPECIAL
}
