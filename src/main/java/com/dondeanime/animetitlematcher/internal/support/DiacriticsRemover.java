package com.dondeanime.animetitlematcher.internal.support;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Removes diacritical marks (accents) from text so that, for example, {@code "Pokémon"} and
 * {@code "Pokemon"} normalise to the same form.
 *
 * <p>The approach is Unicode canonical decomposition (NFD), which splits an accented character
 * into its base letter plus a combining mark, followed by stripping the combining marks. Scripts
 * without combining marks (such as Japanese kana and kanji) are left untouched.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class DiacriticsRemover {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private DiacriticsRemover() {
    }

    /**
     * Returns {@code input} with diacritical marks removed. {@code null} and empty input are
     * returned unchanged.
     */
    public static String removeDiacritics(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(decomposed).replaceAll("");
    }
}
