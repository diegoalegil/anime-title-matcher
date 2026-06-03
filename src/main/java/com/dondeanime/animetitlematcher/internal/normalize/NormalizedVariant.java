package com.dondeanime.animetitlematcher.internal.normalize;

import com.dondeanime.animetitlematcher.domain.TitleVariant;

/**
 * Pairs a source {@link TitleVariant} with its {@link NormalizedTitle}, so the scorer can compare
 * normalised forms while still reporting which original variant matched.
 *
 * <p>This type is part of the internal implementation and is not a stable public API.
 */
public record NormalizedVariant(TitleVariant variant, NormalizedTitle normalized) {
}
