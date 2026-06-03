package com.dondeanime.animetitlematcher.domain;

import java.util.Objects;

/**
 * A single title string together with its provenance.
 *
 * <p>Carrying the {@link Source} lets the matcher explain <em>which</em> AniList and TMDb
 * variants were responsible for a score, and lets the scorer weight canonical titles slightly
 * above alternative ones.
 */
public record TitleVariant(String value, Source source) {

    public TitleVariant {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(source, "source");
    }

    /**
     * Origin of a title. Primary sources are the canonical titles; non-primary sources
     * (synonyms, TMDb alternative titles) are weighted slightly lower during scoring so that,
     * all else being equal, a canonical-title match wins over an alternative-title match.
     */
    public enum Source {
        ROMAJI(true),
        ENGLISH(true),
        NATIVE(true),
        SYNONYM(false),
        TMDB_TITLE(true),
        TMDB_ORIGINAL(true),
        TMDB_ALTERNATIVE(false);

        private final boolean primary;

        Source(boolean primary) {
            this.primary = primary;
        }

        public boolean isPrimary() {
            return primary;
        }
    }

    public boolean isPrimary() {
        return source.isPrimary();
    }
}
