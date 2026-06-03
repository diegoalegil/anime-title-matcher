package com.dondeanime.animetitlematcher.internal.rules;

import java.util.Set;

/**
 * Low-value tokens that carry little matching signal and are removed from the similarity string
 * during normalisation (after any sequence/format markers have already been extracted as metadata).
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 */
public final class TokenRules {

    private static final Set<String> STOPWORDS = Set.of(
            "the", "vol", "vols", "volume", "chapter", "episode", "episodes", "arc");

    private TokenRules() {
    }

    public static boolean isStopword(String token) {
        return token != null && STOPWORDS.contains(token);
    }

    public static Set<String> stopwords() {
        return STOPWORDS;
    }
}
