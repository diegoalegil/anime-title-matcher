package com.dondeanime.animetitlematcher.internal.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Small helpers for working with whitespace-separated tokens.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class TokenUtils {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private TokenUtils() {
    }

    /** Trims {@code input} and collapses every run of whitespace to a single space. */
    public static String collapseWhitespace(String input) {
        if (input == null) {
            return "";
        }
        return WHITESPACE.matcher(input.trim()).replaceAll(" ");
    }

    /** Splits {@code input} into tokens on whitespace, dropping empty tokens. */
    public static List<String> tokenize(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        for (String part : WHITESPACE.split(input.trim())) {
            if (!part.isEmpty()) {
                tokens.add(part);
            }
        }
        return List.copyOf(tokens);
    }

    /** Joins {@code tokens} with single spaces. */
    public static String join(List<String> tokens) {
        return String.join(" ", tokens);
    }
}
