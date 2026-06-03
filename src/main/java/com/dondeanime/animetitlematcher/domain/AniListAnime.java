package com.dondeanime.animetitlematcher.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable snapshot of the AniList side of a match: the titles and metadata the matcher uses
 * to find the most probable TMDb entry.
 *
 * <p>Instances are created through {@link #builder()}. Every field except the titles is
 * optional; unknown numeric metadata is represented as {@code null} and treated as a neutral
 * signal during scoring. The {@code synonyms} list is always non-null and unmodifiable.
 */
public record AniListAnime(
        long id,
        String romajiTitle,
        String englishTitle,
        String nativeTitle,
        List<String> synonyms,
        Integer year,
        AnimeFormat format,
        Integer episodes) {

    public AniListAnime {
        synonyms = synonyms == null
                ? List.of()
                : synonyms.stream().filter(s -> s != null && !s.isBlank()).toList();
        format = format == null ? AnimeFormat.UNKNOWN : format;
    }

    /**
     * Returns every usable title as a {@link TitleVariant}, in priority order: romaji, english,
     * native, then synonyms. Blank titles are skipped.
     */
    public List<TitleVariant> titleVariants() {
        List<TitleVariant> variants = new ArrayList<>();
        addIfPresent(variants, romajiTitle, TitleVariant.Source.ROMAJI);
        addIfPresent(variants, englishTitle, TitleVariant.Source.ENGLISH);
        addIfPresent(variants, nativeTitle, TitleVariant.Source.NATIVE);
        for (String synonym : synonyms) {
            variants.add(new TitleVariant(synonym, TitleVariant.Source.SYNONYM));
        }
        return List.copyOf(variants);
    }

    /** The first non-blank title (romaji, then english, then native), or an empty string. */
    public String primaryTitle() {
        if (romajiTitle != null && !romajiTitle.isBlank()) {
            return romajiTitle;
        }
        if (englishTitle != null && !englishTitle.isBlank()) {
            return englishTitle;
        }
        if (nativeTitle != null && !nativeTitle.isBlank()) {
            return nativeTitle;
        }
        return "";
    }

    private static void addIfPresent(List<TitleVariant> variants, String value, TitleVariant.Source source) {
        if (value != null && !value.isBlank()) {
            variants.add(new TitleVariant(value, source));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link AniListAnime}. */
    public static final class Builder {
        private long id;
        private String romajiTitle;
        private String englishTitle;
        private String nativeTitle;
        private final List<String> synonyms = new ArrayList<>();
        private Integer year;
        private AnimeFormat format = AnimeFormat.UNKNOWN;
        private Integer episodes;

        private Builder() {
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        /** Sets the primary (romaji) title. Convenience alias for {@link #romajiTitle(String)}. */
        public Builder title(String title) {
            this.romajiTitle = title;
            return this;
        }

        public Builder romajiTitle(String romajiTitle) {
            this.romajiTitle = romajiTitle;
            return this;
        }

        public Builder englishTitle(String englishTitle) {
            this.englishTitle = englishTitle;
            return this;
        }

        public Builder nativeTitle(String nativeTitle) {
            this.nativeTitle = nativeTitle;
            return this;
        }

        public Builder synonyms(List<String> synonyms) {
            this.synonyms.clear();
            if (synonyms != null) {
                this.synonyms.addAll(synonyms);
            }
            return this;
        }

        public Builder synonym(String synonym) {
            if (synonym != null) {
                this.synonyms.add(synonym);
            }
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

        public Builder format(AnimeFormat format) {
            this.format = format;
            return this;
        }

        public Builder episodes(Integer episodes) {
            this.episodes = episodes;
            return this;
        }

        public AniListAnime build() {
            return new AniListAnime(id, romajiTitle, englishTitle, nativeTitle, synonyms, year, format, episodes);
        }
    }
}
