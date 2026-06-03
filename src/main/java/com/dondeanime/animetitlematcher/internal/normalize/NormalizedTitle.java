package com.dondeanime.animetitlematcher.internal.normalize;

import com.dondeanime.animetitlematcher.internal.rules.TitleSequence;
import java.util.List;
import java.util.Set;

/**
 * The result of normalising a single title: the original text (never lost), the cleaned
 * {@code normalized} string used for similarity, its ordered {@code tokens} and {@code tokenSet},
 * and the {@link TitleSequence} metadata extracted along the way.
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 */
public record NormalizedTitle(
        String original,
        String normalized,
        List<String> tokens,
        Set<String> tokenSet,
        TitleSequence sequence) {

    public NormalizedTitle {
        original = original == null ? "" : original;
        normalized = normalized == null ? "" : normalized;
        tokens = tokens == null ? List.of() : List.copyOf(tokens);
        tokenSet = tokenSet == null ? Set.of() : Set.copyOf(tokenSet);
        sequence = sequence == null ? TitleSequence.none(normalized) : sequence;
    }

    public boolean isEmpty() {
        return normalized.isEmpty();
    }
}
