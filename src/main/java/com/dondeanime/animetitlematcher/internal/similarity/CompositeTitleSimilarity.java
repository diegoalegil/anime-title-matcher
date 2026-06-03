package com.dondeanime.animetitlematcher.internal.similarity;

import com.dondeanime.animetitlematcher.internal.normalize.NormalizedTitle;
import com.dondeanime.animetitlematcher.internal.support.TokenUtils;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Blends character-level and token-level similarity into a single title-similarity score.
 *
 * <p>Character-level measures (Jaro-Winkler, Levenshtein) catch spelling and punctuation
 * differences; the token component ({@code max} of Jaccard overlap and token-sort ratio) catches
 * word reordering and translation. The weighted blend favours Jaro-Winkler but gives real weight
 * to token overlap so that, e.g., {@code "my hero academia"} and {@code "boku no hero academia"}
 * score moderately high while unrelated titles stay low.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class CompositeTitleSimilarity {

    private static final StringSimilarity LEVENSHTEIN = new LevenshteinSimilarity();
    private static final StringSimilarity JARO_WINKLER = new JaroWinklerSimilarity();

    private static final double JARO_WINKLER_WEIGHT = 0.45;
    private static final double LEVENSHTEIN_WEIGHT = 0.30;
    private static final double TOKEN_WEIGHT = 0.25;

    /** Similarity between two normalised titles, reusing their precomputed tokens. */
    public double similarity(NormalizedTitle a, NormalizedTitle b) {
        if (a == null || b == null) {
            return 0.0;
        }
        return compute(a.normalized(), a.tokens(), a.tokenSet(), b.normalized(), b.tokens(), b.tokenSet());
    }

    /** Similarity between two already-normalised strings (tokens are derived on the fly). */
    public double similarity(String normalizedA, String normalizedB) {
        List<String> tokensA = TokenUtils.tokenize(normalizedA);
        List<String> tokensB = TokenUtils.tokenize(normalizedB);
        return compute(
                normalizedA == null ? "" : normalizedA, tokensA, new LinkedHashSet<>(tokensA),
                normalizedB == null ? "" : normalizedB, tokensB, new LinkedHashSet<>(tokensB));
    }

    private double compute(String stringA, List<String> tokensA, Set<String> tokenSetA,
                           String stringB, List<String> tokensB, Set<String> tokenSetB) {
        boolean aEmpty = stringA == null || stringA.isEmpty();
        boolean bEmpty = stringB == null || stringB.isEmpty();
        if (aEmpty && bEmpty) {
            return 1.0;
        }
        if (aEmpty || bEmpty) {
            return 0.0;
        }
        if (stringA.equals(stringB)) {
            return 1.0;
        }

        // Compare both the strings as-is and their token-sorted forms, taking the better of the
        // two at the character level. This rewards word reordering (Japanese vs English order)
        // without relying on the more generous token-sort ratio alone.
        String sortedA = TokenSetSimilarity.sortedJoin(tokensA);
        String sortedB = TokenSetSimilarity.sortedJoin(tokensB);
        double jaroWinkler = Math.max(
                JARO_WINKLER.similarity(stringA, stringB),
                JARO_WINKLER.similarity(sortedA, sortedB));
        double levenshtein = Math.max(
                LEVENSHTEIN.similarity(stringA, stringB),
                LEVENSHTEIN.similarity(sortedA, sortedB));
        double jaccard = TokenSetSimilarity.jaccard(tokenSetA, tokenSetB);

        double blended = JARO_WINKLER_WEIGHT * jaroWinkler
                + LEVENSHTEIN_WEIGHT * levenshtein
                + TOKEN_WEIGHT * jaccard;
        return Math.max(0.0, Math.min(1.0, blended));
    }
}
