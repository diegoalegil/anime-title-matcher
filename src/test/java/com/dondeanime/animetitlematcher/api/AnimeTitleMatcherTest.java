package com.dondeanime.animetitlematcher.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.AnimeFormat;
import com.dondeanime.animetitlematcher.domain.TmdbMediaType;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnimeTitleMatcherTest {

    private final AnimeTitleMatcher matcher = AnimeTitleMatcher.createDefault();

    private static AniListAnime anime(String title, Integer year, AnimeFormat format) {
        return AniListAnime.builder().id(1).title(title).year(year).format(format).build();
    }

    private static TmdbTitle tmdb(long id, String title, Integer year, TmdbMediaType type) {
        return TmdbTitle.builder().id(id).title(title).year(year).mediaType(type).build();
    }

    @Test
    void matchesAStrongCandidateWithHighConfidence() {
        AniListAnime kimetsu = AniListAnime.builder()
                .id(1).title("Kimetsu no Yaiba")
                .englishTitle("Demon Slayer: Kimetsu no Yaiba")
                .year(2019).format(AnimeFormat.TV).episodes(26)
                .build();
        TmdbTitle candidate = TmdbTitle.builder()
                .id(99).title("Demon Slayer: Kimetsu no Yaiba")
                .year(2019).mediaType(TmdbMediaType.TV).episodeCount(26)
                .build();

        MatchResult result = matcher.findBestMatch(kimetsu, List.of(candidate));

        assertThat(result.isMatched()).isTrue();
        assertThat(result.decision().isConfident()).isTrue();
        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(99);
        assertThat(result.score()).isGreaterThan(0.9);
        assertThat(result.explanation().summary()).isNotBlank();
        assertThat(result.explanation().signalsUsed()).contains("title");
    }

    @Test
    void emptyCandidateListIsNoMatchWithoutThrowing() {
        MatchResult result = matcher.findBestMatch(anime("Naruto", 2002, AnimeFormat.TV), List.of());

        assertThat(result.decision()).isEqualTo(MatchDecision.NO_MATCH);
        assertThat(result.bestCandidate()).isNull();
        assertThat(result.bestMatch()).isEmpty();
        assertThat(result.warnings()).isNotEmpty();
        assertThat(result.explanation().rejectionReason()).isNotBlank();
    }

    @Test
    void picksTheHighestScoringCandidate() {
        AniListAnime frieren = anime("Sousou no Frieren", 2023, AnimeFormat.TV);
        TmdbTitle wrong = tmdb(1, "Naruto", 2002, TmdbMediaType.TV);
        TmdbTitle right = TmdbTitle.builder()
                .id(2).title("Frieren: Beyond Journey's End")
                .originalTitle("Sousou no Frieren")
                .year(2023).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(frieren, List.of(wrong, right));

        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(2);
        assertThat(result.allCandidates()).hasSize(2);
    }

    @Test
    void candidatesAreSortedByScoreWithDeterministicTieBreak() {
        AniListAnime steins = anime("Steins;Gate", 2011, AnimeFormat.TV);
        // Two identical-title candidates: equal scores, so the lower id must come first.
        TmdbTitle high = tmdb(20, "Steins;Gate", 2011, TmdbMediaType.TV);
        TmdbTitle low = tmdb(10, "Steins;Gate", 2011, TmdbMediaType.TV);

        MatchResult result = matcher.findBestMatch(steins, List.of(high, low));

        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(10);
        for (int i = 1; i < result.allCandidates().size(); i++) {
            assertThat(result.allCandidates().get(i - 1).score())
                    .isGreaterThanOrEqualTo(result.allCandidates().get(i).score());
        }
    }

    @Test
    void flagsAmbiguousWhenTwoCandidatesAreNearlyEqual() {
        AniListAnime fate = anime("Fate/stay night", 2006, AnimeFormat.TV);
        TmdbTitle first = tmdb(1, "Fate/stay night", 2006, TmdbMediaType.TV);
        TmdbTitle second = tmdb(2, "Fate stay night", 2006, TmdbMediaType.TV);

        MatchResult result = matcher.findBestMatch(fate, List.of(first, second));

        assertThat(result.decision()).isEqualTo(MatchDecision.AMBIGUOUS);
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void rejectsFormatConflictButStillReportsClosest() {
        AniListAnime series = anime("Example Title", 2019, AnimeFormat.TV);
        TmdbTitle asMovie = tmdb(5, "Example Title", 2019, TmdbMediaType.MOVIE);

        MatchResult result = matcher.findBestMatch(series, List.of(asMovie));

        assertThat(result.decision()).isEqualTo(MatchDecision.NO_MATCH);
        assertThat(result.bestCandidate()).isNotNull();
        assertThat(result.bestCandidate().hardConflict()).isTrue();
    }

    @Test
    void perRequestWeightsOverrideDefaults() {
        AniListAnime anime = anime("Example Title", 2019, AnimeFormat.TV);
        TmdbTitle candidate = tmdb(1, "Example Title", 2019, TmdbMediaType.TV);
        MatchRequest request = new MatchRequest(anime, List.of(candidate),
                new ScoringWeights(1.0, 0.0, 0.0, 0.0, 0.0), MatchThresholds.defaults());

        MatchResult result = matcher.match(request);

        assertThat(result.score()).isBetween(0.0, 1.0);
        assertThat(result.isMatched()).isTrue();
    }

    @Test
    void everyCandidateScoreStaysWithinUnitRange() {
        AniListAnime anime = anime("Cowboy Bebop", 1998, AnimeFormat.TV);
        List<TmdbTitle> candidates = List.of(
                tmdb(1, "Cowboy Bebop", 1998, TmdbMediaType.TV),
                tmdb(2, "Naruto", 2002, TmdbMediaType.TV),
                tmdb(3, "Spirited Away", 2001, TmdbMediaType.MOVIE));

        MatchResult result = matcher.findBestMatch(anime, candidates);

        for (MatchCandidate candidate : result.allCandidates()) {
            assertThat(candidate.score()).isBetween(0.0, 1.0);
        }
    }
}
