package com.dondeanime.animetitlematcher.internal.similarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Order-independent, token-based similarity, used to recognise titles that share words but in a
 * different order (for example a Japanese romaji title versus its English translation).
 *
 * <p>{@link #jaccard(Set, Set)} measures set overlap; {@link #tokenSortRatio(List, List,
 * StringSimilarity)} sorts the tokens of each side, joins them and compares the results with a
 * character-level similarity, which tolerates spelling differences as well as reordering.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class TokenSetSimilarity {

    private TokenSetSimilarity() {
    }

    /** Jaccard index of two token sets: {@code |A ∩ B| / |A ∪ B|}. Two empty sets score {@code 1.0}. */
    public static double jaccard(Set<String> a, Set<String> b) {
        boolean aEmpty = a == null || a.isEmpty();
        boolean bEmpty = b == null || b.isEmpty();
        if (aEmpty && bEmpty) {
            return 1.0;
        }
        if (aEmpty || bEmpty) {
            return 0.0;
        }
        int intersection = 0;
        for (String token : a) {
            if (b.contains(token)) {
                intersection++;
            }
        }
        int union = a.size() + b.size() - intersection;
        return union == 0 ? 1.0 : (double) intersection / union;
    }

    /** Sorts each token list, joins it and compares the two with {@code charSimilarity}. */
    public static double tokenSortRatio(List<String> a, List<String> b, StringSimilarity charSimilarity) {
        return charSimilarity.similarity(sortedJoin(a), sortedJoin(b));
    }

    /** Joins {@code tokens} in sorted order, so word reordering yields the same string. */
    public static String sortedJoin(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return "";
        }
        List<String> sorted = new ArrayList<>(tokens);
        Collections.sort(sorted);
        return String.join(" ", sorted);
    }
}
