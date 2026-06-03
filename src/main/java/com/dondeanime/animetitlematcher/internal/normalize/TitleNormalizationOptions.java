package com.dondeanime.animetitlematcher.internal.normalize;

/**
 * Toggles for the {@link TitleNormalizer} pipeline. Every step can be turned off independently,
 * which keeps normalisation testable and lets callers tune behaviour.
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 *
 * @param lowercase            lower-case the title
 * @param removeDiacritics     strip accents (é → e)
 * @param removeParentheticals drop bracketed segments such as {@code "(TV)"}
 * @param expandAmpersand      replace {@code "&"} with {@code "and"}
 * @param removeApostrophes    delete apostrophes without splitting words
 * @param convertRomanNumerals turn Roman numeral tokens into Arabic ones
 * @param detectSequence       extract season/part/cour/format markers into metadata
 * @param removeStopwords      drop low-value tokens from the similarity string
 */
public record TitleNormalizationOptions(
        boolean lowercase,
        boolean removeDiacritics,
        boolean removeParentheticals,
        boolean expandAmpersand,
        boolean removeApostrophes,
        boolean convertRomanNumerals,
        boolean detectSequence,
        boolean removeStopwords) {

    /** Every step enabled. */
    public static TitleNormalizationOptions defaults() {
        return new TitleNormalizationOptions(true, true, true, true, true, true, true, true);
    }
}
