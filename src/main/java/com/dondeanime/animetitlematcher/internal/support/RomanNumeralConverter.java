package com.dondeanime.animetitlematcher.internal.support;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Converts Roman numeral tokens (e.g. {@code "ii"}, {@code "iv"}, {@code "xiii"}) into their
 * Arabic equivalents so that {@code "Macross II"} and {@code "Macross 2"} normalise alike.
 *
 * <p>Validation is done by a round trip: a token is accepted only if re-encoding its parsed
 * value yields the original token, which rejects malformed numerals such as {@code "iiii"} or
 * {@code "vv"}. The single ambiguous letters {@code i, l, c, d, m} are left untouched because in
 * titles they are far more often ordinary words or initials than numerals; {@code v} and
 * {@code x}, common sequel markers, are still converted.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class RomanNumeralConverter {

    private static final Map<Character, Integer> VALUES = Map.of(
            'i', 1, 'v', 5, 'x', 10, 'l', 50, 'c', 100, 'd', 500, 'm', 1000);

    private static final int[] ARABIC = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN = {"m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};

    private static final Set<String> AMBIGUOUS_SINGLE = Set.of("i", "l", "c", "d", "m");

    private RomanNumeralConverter() {
    }

    /** Returns whether {@code token} is a well-formed Roman numeral (case-insensitive). */
    public static boolean isRomanNumeral(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String lower = token.toLowerCase(Locale.ROOT);
        for (int i = 0; i < lower.length(); i++) {
            if (!VALUES.containsKey(lower.charAt(i))) {
                return false;
            }
        }
        int value = parse(lower);
        return value > 0 && toRoman(value).equals(lower);
    }

    /**
     * Converts {@code token} to its Arabic form when it is an unambiguous Roman numeral;
     * otherwise returns the token unchanged. {@code null} is returned unchanged.
     */
    public static String convertToken(String token) {
        if (token == null) {
            return null;
        }
        String lower = token.toLowerCase(Locale.ROOT);
        if (AMBIGUOUS_SINGLE.contains(lower)) {
            return token;
        }
        if (isRomanNumeral(lower)) {
            return Integer.toString(parse(lower));
        }
        return token;
    }

    private static int parse(String roman) {
        int total = 0;
        int previous = 0;
        for (int i = roman.length() - 1; i >= 0; i--) {
            int value = VALUES.get(roman.charAt(i));
            if (value < previous) {
                total -= value;
            } else {
                total += value;
                previous = value;
            }
        }
        return total;
    }

    private static String toRoman(int number) {
        StringBuilder builder = new StringBuilder();
        int remaining = number;
        for (int i = 0; i < ARABIC.length; i++) {
            while (remaining >= ARABIC[i]) {
                builder.append(ROMAN[i]);
                remaining -= ARABIC[i];
            }
        }
        return builder.toString();
    }
}
