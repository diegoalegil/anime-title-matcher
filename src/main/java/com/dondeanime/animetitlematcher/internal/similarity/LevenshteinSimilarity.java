package com.dondeanime.animetitlematcher.internal.similarity;

/**
 * Similarity based on the Levenshtein edit distance (the minimum number of single-character
 * insertions, deletions or substitutions to turn one string into another).
 *
 * <p>The distance is computed with a two-row dynamic-programming table: {@code O(n * m)} time and
 * {@code O(min(n, m))} space, where {@code n} and {@code m} are the string lengths. Similarity is
 * the distance normalised by the longer length: {@code 1 - distance / maxLength}, so identical
 * strings score {@code 1.0} and two empty strings also score {@code 1.0}.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class LevenshteinSimilarity implements StringSimilarity {

    @Override
    public double similarity(String a, String b) {
        return score(a, b);
    }

    /** Normalised similarity in {@code [0.0, 1.0]}. */
    public static double score(String a, String b) {
        String first = a == null ? "" : a;
        String second = b == null ? "" : b;
        if (first.equals(second)) {
            return 1.0;
        }
        int maxLength = Math.max(first.length(), second.length());
        if (maxLength == 0) {
            return 1.0;
        }
        return 1.0 - (double) distance(first, second) / maxLength;
    }

    /** The Levenshtein edit distance between {@code a} and {@code b}. */
    public static int distance(String a, String b) {
        String longer = a == null ? "" : a;
        String shorter = b == null ? "" : b;
        if (longer.equals(shorter)) {
            return 0;
        }
        // Keep the inner row as short as possible for O(min(n, m)) space.
        if (longer.length() < shorter.length()) {
            String swap = longer;
            longer = shorter;
            shorter = swap;
        }
        int shortLength = shorter.length();
        if (shortLength == 0) {
            return longer.length();
        }

        int[] previous = new int[shortLength + 1];
        int[] current = new int[shortLength + 1];
        for (int j = 0; j <= shortLength; j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= longer.length(); i++) {
            current[0] = i;
            char longerChar = longer.charAt(i - 1);
            for (int j = 1; j <= shortLength; j++) {
                int substitutionCost = longerChar == shorter.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + substitutionCost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[shortLength];
    }
}
