package com.dondeanime.animetitlematcher.internal.normalize;

import com.dondeanime.animetitlematcher.internal.rules.SeasonDetector;
import com.dondeanime.animetitlematcher.internal.rules.TitleSequence;
import com.dondeanime.animetitlematcher.internal.rules.TokenRules;
import com.dondeanime.animetitlematcher.internal.support.DiacriticsRemover;
import com.dondeanime.animetitlematcher.internal.support.RomanNumeralConverter;
import com.dondeanime.animetitlematcher.internal.support.TextSanitizer;
import com.dondeanime.animetitlematcher.internal.support.TokenUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Turns a raw anime title into a {@link NormalizedTitle} through a configurable pipeline:
 * lower-casing, accent stripping, bracket/ampersand/apostrophe handling, symbol removal, Roman
 * numeral conversion, season/format extraction and stopword removal.
 *
 * <p>The original title is always preserved and the extracted {@link TitleSequence} metadata is
 * kept on the result, so no useful information is irreversibly destroyed.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class TitleNormalizer {

    private final TitleNormalizationOptions options;

    public TitleNormalizer() {
        this(TitleNormalizationOptions.defaults());
    }

    public TitleNormalizer(TitleNormalizationOptions options) {
        this.options = options == null ? TitleNormalizationOptions.defaults() : options;
    }

    public NormalizedTitle normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return new NormalizedTitle(raw == null ? "" : raw, "", List.of(), Set.of(), TitleSequence.none(""));
        }

        String working = raw;
        if (options.lowercase()) {
            working = working.toLowerCase(Locale.ROOT);
        }
        if (options.removeDiacritics()) {
            working = DiacriticsRemover.removeDiacritics(working);
        }
        if (options.removeParentheticals()) {
            working = TextSanitizer.removeParentheticals(working);
        }
        if (options.expandAmpersand()) {
            working = TextSanitizer.expandAmpersand(working);
        }
        if (options.removeApostrophes()) {
            working = TextSanitizer.removeApostrophes(working);
        }
        working = TokenUtils.collapseWhitespace(TextSanitizer.stripSymbols(working));

        if (options.convertRomanNumerals()) {
            List<String> converted = new ArrayList<>();
            for (String token : TokenUtils.tokenize(working)) {
                converted.add(RomanNumeralConverter.convertToken(token));
            }
            working = TokenUtils.join(converted);
        }

        TitleSequence sequence;
        String base;
        if (options.detectSequence()) {
            sequence = SeasonDetector.detect(working);
            base = sequence.baseTitle();
        } else {
            sequence = TitleSequence.none(working);
            base = working;
        }

        List<String> tokens = TokenUtils.tokenize(base);
        if (options.removeStopwords()) {
            List<String> filtered = tokens.stream().filter(token -> !TokenRules.isStopword(token)).toList();
            // Keep the original tokens if stripping stopwords would leave nothing to match on.
            if (!filtered.isEmpty()) {
                tokens = filtered;
            }
        }

        String normalized = TokenUtils.join(tokens);
        Set<String> tokenSet = new LinkedHashSet<>(tokens);
        return new NormalizedTitle(raw, normalized, tokens, tokenSet, sequence);
    }
}
