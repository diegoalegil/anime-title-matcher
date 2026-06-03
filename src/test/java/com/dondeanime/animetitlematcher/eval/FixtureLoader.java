package com.dondeanime.animetitlematcher.eval;

import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.AnimeFormat;
import com.dondeanime.animetitlematcher.domain.TmdbMediaType;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

/**
 * Loads evaluation fixtures from classpath JSON resources (test scope only). Uses Jackson; the
 * library itself has no runtime JSON dependency.
 */
public final class FixtureLoader {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private FixtureLoader() {
    }

    /** Loads labelled matching cases, e.g. from {@code "/fixtures/matching-cases.json"}. */
    public static List<EvaluationCase> loadCases(String resourcePath) {
        return read(resourcePath, CaseFile.class).cases().stream().map(FixtureLoader::toCase).toList();
    }

    /** Loads a standalone list of AniList anime, e.g. from {@code "/fixtures/anilist-sample.json"}. */
    public static List<AniListAnime> loadAnilistSample(String resourcePath) {
        return Arrays.stream(read(resourcePath, AnimeDto[].class)).map(FixtureLoader::toAnime).toList();
    }

    /** Loads a standalone list of TMDb candidates, e.g. from {@code "/fixtures/tmdb-candidates-sample.json"}. */
    public static List<TmdbTitle> loadTmdbSample(String resourcePath) {
        return Arrays.stream(read(resourcePath, CandidateDto[].class)).map(FixtureLoader::toTmdb).toList();
    }

    private static <T> T read(String resourcePath, Class<T> type) {
        try (InputStream in = FixtureLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Fixture not found on classpath: " + resourcePath);
            }
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read fixture: " + resourcePath, e);
        }
    }

    private static EvaluationCase toCase(CaseDto dto) {
        List<TmdbTitle> candidates = dto.candidates() == null
                ? List.of()
                : dto.candidates().stream().map(FixtureLoader::toTmdb).toList();
        return new EvaluationCase(dto.name(), toAnime(dto.anilist()), candidates, dto.expectedTmdbId(), dto.expectMatch());
    }

    private static AniListAnime toAnime(AnimeDto dto) {
        return AniListAnime.builder()
                .id(dto.id())
                .title(dto.romajiTitle())
                .englishTitle(dto.englishTitle())
                .nativeTitle(dto.nativeTitle())
                .synonyms(dto.synonyms())
                .year(dto.year())
                .format(AnimeFormat.parse(dto.format()))
                .episodes(dto.episodes())
                .build();
    }

    private static TmdbTitle toTmdb(CandidateDto dto) {
        return TmdbTitle.builder()
                .id(dto.id())
                .title(dto.title())
                .originalTitle(dto.originalTitle())
                .alternativeTitles(dto.alternativeTitles())
                .year(dto.year())
                .mediaType(TmdbMediaType.fromTmdbString(dto.mediaType()))
                .episodeCount(dto.episodeCount())
                .build();
    }

    record CaseFile(List<CaseDto> cases) {
    }

    record CaseDto(String name, AnimeDto anilist, List<CandidateDto> candidates, Long expectedTmdbId, boolean expectMatch) {
    }

    record AnimeDto(long id, String romajiTitle, String englishTitle, String nativeTitle,
                    List<String> synonyms, Integer year, String format, Integer episodes) {
    }

    record CandidateDto(long id, String title, String originalTitle, List<String> alternativeTitles,
                        Integer year, String mediaType, Integer episodeCount) {
    }
}
