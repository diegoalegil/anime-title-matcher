package com.dondeanime.animetitlematcher.internal.rules;

/**
 * Structured sequence metadata extracted from a title by {@link SeasonDetector}: which season,
 * part and cour it refers to, whether it is marked as a "final" season/part, and any format hint.
 * The {@code baseTitle} is the title with those markers removed, ready for similarity comparison.
 *
 * <p>This metadata is deliberately preserved (rather than silently discarded during normalisation)
 * so the scorer can reward or penalise season/part agreement between an AniList anime and a TMDb
 * candidate. {@code null} season/part/cour means "not specified".
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 */
public record TitleSequence(
        Integer season,
        Integer part,
        Integer cour,
        boolean finalMarker,
        FormatHint formatHint,
        String baseTitle) {

    public TitleSequence {
        formatHint = formatHint == null ? FormatHint.NONE : formatHint;
        baseTitle = baseTitle == null ? "" : baseTitle;
    }

    /** Whether any season, part, cour or final marker was detected. */
    public boolean hasSequenceInfo() {
        return season != null || part != null || cour != null || finalMarker;
    }

    /** A sequence carrying no markers, wrapping {@code baseTitle} unchanged. */
    public static TitleSequence none(String baseTitle) {
        return new TitleSequence(null, null, null, false, FormatHint.NONE, baseTitle == null ? "" : baseTitle);
    }
}
