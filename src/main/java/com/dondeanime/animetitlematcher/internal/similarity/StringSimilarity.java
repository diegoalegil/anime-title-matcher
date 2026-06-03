package com.dondeanime.animetitlematcher.internal.similarity;

/**
 * A measure of how similar two strings are, always in the range {@code [0.0, 1.0]} where
 * {@code 1.0} means identical and {@code 0.0} means completely different. Implementations must be
 * null-safe and symmetric.
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 */
@FunctionalInterface
public interface StringSimilarity {

    double similarity(String a, String b);
}
