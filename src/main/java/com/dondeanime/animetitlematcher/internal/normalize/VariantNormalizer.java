package com.dondeanime.animetitlematcher.internal.normalize;

import com.dondeanime.animetitlematcher.domain.TitleVariant;
import java.util.List;

/**
 * Normalises a list of {@link TitleVariant}s with a shared {@link TitleNormalizer}, dropping any
 * that normalise to nothing so that empty titles cannot spuriously match each other.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class VariantNormalizer {

    private final TitleNormalizer normalizer;

    public VariantNormalizer(TitleNormalizer normalizer) {
        this.normalizer = normalizer == null ? new TitleNormalizer() : normalizer;
    }

    public List<NormalizedVariant> normalize(List<TitleVariant> variants) {
        if (variants == null) {
            return List.of();
        }
        return variants.stream()
                .map(variant -> new NormalizedVariant(variant, normalizer.normalize(variant.value())))
                .filter(normalized -> !normalized.normalized().isEmpty())
                .toList();
    }
}
