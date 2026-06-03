package com.dondeanime.animetitlematcher.internal.score;

import static org.assertj.core.api.Assertions.assertThat;

import com.dondeanime.animetitlematcher.api.MatchThresholds;
import com.dondeanime.animetitlematcher.api.ScoringWeights;
import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.AnimeFormat;
import com.dondeanime.animetitlematcher.domain.TmdbMediaType;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import com.dondeanime.animetitlematcher.internal.normalize.NormalizedVariant;
import com.dondeanime.animetitlematcher.internal.normalize.TitleNormalizer;
import com.dondeanime.animetitlematcher.internal.normalize.VariantNormalizer;
import com.dondeanime.animetitlematcher.internal.similarity.CompositeTitleSimilarity;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchScorerTest {

    private final VariantNormalizer variantNormalizer = new VariantNormalizer(new TitleNormalizer());
    private final MatchScorer scorer = new MatchScorer(
            new CompositeTitleSimilarity(), ScoringWeights.defaults(), MatchThresholds.defaults());

    private ScoreResult score(AniListAnime anime, TmdbTitle tmdb) {
        List<NormalizedVariant> anilistVariants = variantNormalizer.normalize(anime.titleVariants());
        List<NormalizedVariant> tmdbVariants = variantNormalizer.normalize(tmdb.titleVariants());
        return scorer.score(anime, anilistVariants, tmdb, tmdbVariants);
    }

    @Test
    void exactTitleWithMatchingMetadataScoresVeryHigh() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Kimetsu no Yaiba")
                .englishTitle("Demon Slayer: Kimetsu no Yaiba")
                .year(2019).format(AnimeFormat.TV).episodes(26)
                .build();
        TmdbTitle tmdb = TmdbTitle.builder()
                .id(10).title("Demon Slayer: Kimetsu no Yaiba")
                .year(2019).mediaType(TmdbMediaType.TV).episodeCount(26)
                .build();

        ScoreResult result = score(anime, tmdb);
        assertThat(result.finalScore()).isGreaterThan(0.95);
        assertThat(result.exactMatch()).isTrue();
        assertThat(result.hardConflict()).isFalse();
        assertThat(result.matchedTmdbVariant()).isNotNull();
    }

    @Test
    void formatConflictCapsTheScoreAndFlagsHardConflict() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Example Title").year(2019).format(AnimeFormat.TV).build();
        TmdbTitle movie = TmdbTitle.builder()
                .id(10).title("Example Title").year(2019).mediaType(TmdbMediaType.MOVIE).build();

        ScoreResult result = score(anime, movie);
        assertThat(result.hardConflict()).isTrue();
        assertThat(result.finalScore()).isLessThanOrEqualTo(0.45);
        assertThat(result.breakdown().formatScore()).isEqualTo(0.0);
        assertThat(result.breakdown().penaltyScore()).isGreaterThan(0.0);
    }

    @Test
    void ovaAgainstTvIsNotCapped() {
        AniListAnime ova = AniListAnime.builder()
                .id(1).title("Example Title").year(2019).format(AnimeFormat.OVA).build();
        TmdbTitle tv = TmdbTitle.builder()
                .id(10).title("Example Title").year(2019).mediaType(TmdbMediaType.TV).build();

        ScoreResult result = score(ova, tv);
        assertThat(result.hardConflict()).isFalse();
        assertThat(result.finalScore()).isGreaterThan(0.85);
    }

    @Test
    void sameYearScoresHigherThanFarYear() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Example Title").year(2019).format(AnimeFormat.TV).build();
        TmdbTitle sameYear = TmdbTitle.builder()
                .id(10).title("Example Title").year(2019).mediaType(TmdbMediaType.TV).build();
        TmdbTitle farYear = TmdbTitle.builder()
                .id(11).title("Example Title").year(2010).mediaType(TmdbMediaType.TV).build();

        assertThat(score(anime, sameYear).finalScore())
                .isGreaterThan(score(anime, farYear).finalScore());
    }

    @Test
    void usesAlternativeTitlesWhenPrimaryTitlesDiffer() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Shingeki no Kyojin")
                .synonym("Attack on Titan")
                .year(2013).format(AnimeFormat.TV).build();
        TmdbTitle tmdb = TmdbTitle.builder()
                .id(10).title("Attack on Titan")
                .year(2013).mediaType(TmdbMediaType.TV).build();

        ScoreResult result = score(anime, tmdb);
        assertThat(result.breakdown().alternativeTitleScore()).isGreaterThan(0.9);
        assertThat(result.breakdown().titleScore()).isLessThan(result.breakdown().alternativeTitleScore());
        assertThat(result.finalScore()).isGreaterThan(0.8);
    }

    @Test
    void seasonSignalIsPresentWhenBothSidesCarrySeasons() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Jujutsu Kaisen 2nd Season").year(2023).format(AnimeFormat.TV).build();
        TmdbTitle tmdb = TmdbTitle.builder()
                .id(10).title("Jujutsu Kaisen Season 2").year(2023).mediaType(TmdbMediaType.TV).build();

        ScoreResult result = score(anime, tmdb);
        assertThat(result.breakdown().seasonScore()).isNotNull();
        assertThat(result.breakdown().seasonScore()).isEqualTo(1.0);
    }

    @Test
    void unrelatedTitlesScoreLow() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Naruto").year(2002).format(AnimeFormat.TV).build();
        TmdbTitle tmdb = TmdbTitle.builder()
                .id(10).title("Spirited Away").year(2001).mediaType(TmdbMediaType.MOVIE).build();

        assertThat(score(anime, tmdb).finalScore()).isLessThan(0.55);
    }

    @Test
    void finalScoreStaysWithinRangeAndIsDeterministic() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Steins;Gate").year(2011).format(AnimeFormat.TV).episodes(24).build();
        TmdbTitle tmdb = TmdbTitle.builder()
                .id(10).title("Steins;Gate").originalTitle("Steins Gate")
                .year(2011).mediaType(TmdbMediaType.TV).episodeCount(24).build();

        double first = score(anime, tmdb).finalScore();
        double second = score(anime, tmdb).finalScore();
        assertThat(first).isBetween(0.0, 1.0).isEqualTo(second);
    }
}
