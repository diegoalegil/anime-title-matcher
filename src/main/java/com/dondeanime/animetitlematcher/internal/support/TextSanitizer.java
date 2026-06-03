package com.dondeanime.animetitlematcher.internal.support;

import java.util.regex.Pattern;

/**
 * String-level cleanup steps shared by the title normaliser.
 *
 * <p>Each method is null-safe (a {@code null} argument yields an empty string) and leaves letters
 * and digits of any script intact, so Japanese native titles survive cleanup while punctuation is
 * removed. Apostrophes are deleted rather than spaced (so {@code "journey's"} becomes
 * {@code "journeys"}), while every other symbol becomes a space to avoid gluing words together.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class TextSanitizer {

    private static final Pattern PARENTHETICAL = Pattern.compile("[(\\[{][^)\\]}]*[)\\]}]");
    private static final Pattern APOSTROPHES = Pattern.compile("['’‘`´\"“”]");
    private static final Pattern NON_LETTER_DIGIT = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private TextSanitizer() {
    }

    /** Removes bracketed segments such as {@code "(TV)"} or {@code "[2021]"}. */
    public static String removeParentheticals(String input) {
        if (input == null) {
            return "";
        }
        return PARENTHETICAL.matcher(input).replaceAll(" ");
    }

    /** Replaces {@code "&"} with the word {@code "and"}. */
    public static String expandAmpersand(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", " and ");
    }

    /** Deletes apostrophes and quotation marks without inserting a space. */
    public static String removeApostrophes(String input) {
        if (input == null) {
            return "";
        }
        return APOSTROPHES.matcher(input).replaceAll("");
    }

    /** Replaces every character that is not a letter, digit or whitespace with a space. */
    public static String stripSymbols(String input) {
        if (input == null) {
            return "";
        }
        return NON_LETTER_DIGIT.matcher(input).replaceAll(" ");
    }
}
