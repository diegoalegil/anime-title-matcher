package com.dondeanime.animetitlematcher.internal.similarity;

/**
 * Jaro-Winkler string similarity. The Jaro score rewards matching characters within a sliding
 * window and penalises transpositions; the Winkler adjustment boosts strings that share a common
 * prefix (up to four characters), which suits titles that begin the same way.
 *
 * <p>Identical strings (including two empty strings) score {@code 1.0}; a non-empty string compared
 * with an empty one scores {@code 0.0}.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class JaroWinklerSimilarity implements StringSimilarity {

    private static final double PREFIX_SCALE = 0.1;
    private static final int MAX_PREFIX = 4;

    @Override
    public double similarity(String a, String b) {
        return jaroWinkler(a, b);
    }

    public static double jaroWinkler(String a, String b) {
        String first = a == null ? "" : a;
        String second = b == null ? "" : b;
        double jaro = jaro(first, second);
        int prefix = commonPrefixLength(first, second);
        return jaro + prefix * PREFIX_SCALE * (1.0 - jaro);
    }

    public static double jaro(String a, String b) {
        String first = a == null ? "" : a;
        String second = b == null ? "" : b;
        if (first.equals(second)) {
            return 1.0;
        }
        int firstLength = first.length();
        int secondLength = second.length();
        if (firstLength == 0 || secondLength == 0) {
            return 0.0;
        }

        int matchWindow = Math.max(0, Math.max(firstLength, secondLength) / 2 - 1);
        boolean[] firstMatched = new boolean[firstLength];
        boolean[] secondMatched = new boolean[secondLength];

        int matches = 0;
        for (int i = 0; i < firstLength; i++) {
            int start = Math.max(0, i - matchWindow);
            int end = Math.min(i + matchWindow + 1, secondLength);
            for (int j = start; j < end; j++) {
                if (secondMatched[j] || first.charAt(i) != second.charAt(j)) {
                    continue;
                }
                firstMatched[i] = true;
                secondMatched[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) {
            return 0.0;
        }

        double transpositions = 0;
        int k = 0;
        for (int i = 0; i < firstLength; i++) {
            if (!firstMatched[i]) {
                continue;
            }
            while (!secondMatched[k]) {
                k++;
            }
            if (first.charAt(i) != second.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        transpositions /= 2.0;

        double matchCount = matches;
        return (matchCount / firstLength
                + matchCount / secondLength
                + (matchCount - transpositions) / matchCount) / 3.0;
    }

    private static int commonPrefixLength(String a, String b) {
        int limit = Math.min(MAX_PREFIX, Math.min(a.length(), b.length()));
        int prefix = 0;
        while (prefix < limit && a.charAt(prefix) == b.charAt(prefix)) {
            prefix++;
        }
        return prefix;
    }
}
