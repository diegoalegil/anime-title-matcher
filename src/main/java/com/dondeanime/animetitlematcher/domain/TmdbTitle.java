package com.dondeanime.animetitlematcher.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable snapshot of a TMDb candidate that an {@link AniListAnime} may correspond to.
 *
 * <p>Instances can be created with the canonical constructor, the {@linkplain #TmdbTitle(long,
 * String, String, Integer, TmdbMediaType, Integer) convenience constructor}, or {@link
 * #builder()}. The {@code alternativeTitles} list is always non-null and unmodifiable, and
 * {@code mediaType} defaults to {@link TmdbMediaType#UNKNOWN}.
 */
public record TmdbTitle(
        long id,
        String title,
        String originalTitle,
        List<String> alternativeTitles,
        Integer year,
        TmdbMediaType mediaType,
        Integer episodeCount,
        String originalLanguage,
        Double popularity) {

    public TmdbTitle {
        alternativeTitles = alternativeTitles == null
                ? List.of()
                : alternativeTitles.stream().filter(s -> s != null && !s.isBlank()).toList();
        mediaType = mediaType == null ? TmdbMediaType.UNKNOWN : mediaType;
    }

    /** Convenience constructor for the common case (no alternative titles, language or popularity). */
    public TmdbTitle(long id, String title, String originalTitle, Integer year,
                     TmdbMediaType mediaType, Integer episodeCount) {
        this(id, title, originalTitle, List.of(), year, mediaType, episodeCount, null, null);
    }

    /**
     * Returns every usable title as a {@link TitleVariant}: the main title, the original title
     * (when it differs), then alternative titles. Blank titles are skipped.
     */
    public List<TitleVariant> titleVariants() {
        List<TitleVariant> variants = new ArrayList<>();
        addIfPresent(variants, title, TitleVariant.Source.TMDB_TITLE);
        if (originalTitle != null && !originalTitle.isBlank() && !originalTitle.equalsIgnoreCase(title)) {
            variants.add(new TitleVariant(originalTitle, TitleVariant.Source.TMDB_ORIGINAL));
        }
        for (String alternativeTitle : alternativeTitles) {
            variants.add(new TitleVariant(alternativeTitle, TitleVariant.Source.TMDB_ALTERNATIVE));
        }
        return List.copyOf(variants);
    }

    private static void addIfPresent(List<TitleVariant> variants, String value, TitleVariant.Source source) {
        if (value != null && !value.isBlank()) {
            variants.add(new TitleVariant(value, source));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link TmdbTitle}. */
    public static final class Builder {
        private long id;
        private String title;
        private String originalTitle;
        private final List<String> alternativeTitles = new ArrayList<>();
        private Integer year;
        private TmdbMediaType mediaType = TmdbMediaType.UNKNOWN;
        private Integer episodeCount;
        private String originalLanguage;
        private Double popularity;

        private Builder() {
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder originalTitle(String originalTitle) {
            this.originalTitle = originalTitle;
            return this;
        }

        public Builder alternativeTitles(List<String> alternativeTitles) {
            this.alternativeTitles.clear();
            if (alternativeTitles != null) {
                this.alternativeTitles.addAll(alternativeTitles);
            }
            return this;
        }

        public Builder alternativeTitle(String alternativeTitle) {
            if (alternativeTitle != null) {
                this.alternativeTitles.add(alternativeTitle);
            }
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

        public Builder mediaType(TmdbMediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder episodeCount(Integer episodeCount) {
            this.episodeCount = episodeCount;
            return this;
        }

        public Builder originalLanguage(String originalLanguage) {
            this.originalLanguage = originalLanguage;
            return this;
        }

        public Builder popularity(Double popularity) {
            this.popularity = popularity;
            return this;
        }

        public TmdbTitle build() {
            return new TmdbTitle(id, title, originalTitle, alternativeTitles, year,
                    mediaType, episodeCount, originalLanguage, popularity);
        }
    }
}
