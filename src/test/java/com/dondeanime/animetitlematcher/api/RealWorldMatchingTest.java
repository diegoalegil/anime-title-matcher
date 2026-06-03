package com.dondeanime.animetitlematcher.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.AnimeFormat;
import com.dondeanime.animetitlematcher.domain.TmdbMediaType;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * End-to-end checks using real, difficult AniList/TMDb title pairs. These assert specific
 * behaviours (alternative titles, season stripping, year disambiguation, format vetoes, ambiguity,
 * no-match); broad accuracy is measured separately by the fixture-driven evaluator.
 */
class RealWorldMatchingTest {

    private final AnimeTitleMatcher matcher = AnimeTitleMatcher.createDefault();

    @Test
    void demonSlayerMatchesAcrossEnglishAndNativeTitles() {
        AniListAnime anime = AniListAnime.builder()
                .id(101922).title("Kimetsu no Yaiba")
                .englishTitle("Demon Slayer: Kimetsu no Yaiba").nativeTitle("鬼滅の刃")
                .year(2019).format(AnimeFormat.TV).build();
        TmdbTitle right = TmdbTitle.builder()
                .id(85937).title("Demon Slayer: Kimetsu no Yaiba").originalTitle("鬼滅の刃")
                .year(2019).mediaType(TmdbMediaType.TV).build();
        TmdbTitle wrong = TmdbTitle.builder().id(1).title("Naruto").year(2002).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(wrong, right));

        assertThat(result.isMatched()).isTrue();
        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(85937);
    }

    @Test
    void attackOnTitanMatchesThroughAnAlternativeTitle() {
        AniListAnime anime = AniListAnime.builder()
                .id(16498).title("Shingeki no Kyojin")
                .synonym("Attack on Titan").nativeTitle("進撃の巨人")
                .year(2013).format(AnimeFormat.TV).build();
        TmdbTitle right = TmdbTitle.builder()
                .id(1429).title("Attack on Titan").year(2013).mediaType(TmdbMediaType.TV).build();
        TmdbTitle wrong = TmdbTitle.builder().id(2).title("Bleach").year(2004).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(wrong, right));

        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(1429);
        assertThat(result.isMatched()).isTrue();
        assertThat(result.bestCandidate().breakdown().alternativeTitleScore()).isGreaterThan(0.9);
    }

    @Test
    void myHeroAcademiaMatchesThroughEnglishTitle() {
        AniListAnime anime = AniListAnime.builder()
                .id(21459).title("Boku no Hero Academia")
                .englishTitle("My Hero Academia").year(2016).format(AnimeFormat.TV).build();
        TmdbTitle right = TmdbTitle.builder()
                .id(65930).title("My Hero Academia").year(2016).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(right));

        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(65930);
        assertThat(result.decision().isConfident()).isTrue();
    }

    @Test
    void jujutsuKaisenSecondSeasonMatchesTheShowEntry() {
        AniListAnime anime = AniListAnime.builder()
                .id(145064).title("Jujutsu Kaisen 2nd Season")
                .englishTitle("Jujutsu Kaisen Season 2").year(2023).format(AnimeFormat.TV).build();
        TmdbTitle show = TmdbTitle.builder()
                .id(95479).title("Jujutsu Kaisen").year(2020).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(show));

        assertThat(result.isMatched()).isTrue();
        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(95479);
    }

    @Test
    void fullmetalBrotherhoodPrefersTheCorrectYearOverTheOlderSeries() {
        AniListAnime anime = AniListAnime.builder()
                .id(5114).title("Fullmetal Alchemist: Brotherhood")
                .englishTitle("Fullmetal Alchemist: Brotherhood").year(2009).format(AnimeFormat.TV).build();
        TmdbTitle brotherhood = TmdbTitle.builder()
                .id(31911).title("Fullmetal Alchemist: Brotherhood").year(2009).mediaType(TmdbMediaType.TV).build();
        TmdbTitle original2003 = TmdbTitle.builder()
                .id(30991).title("Fullmetal Alchemist").year(2003).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(original2003, brotherhood));

        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(31911);
        assertThat(result.isMatched()).isTrue();
    }

    @Test
    void aSilentVoiceMovieMatches() {
        AniListAnime anime = AniListAnime.builder()
                .id(20954).title("Koe no Katachi")
                .englishTitle("A Silent Voice").year(2016).format(AnimeFormat.MOVIE).build();
        TmdbTitle right = TmdbTitle.builder()
                .id(378064).title("A Silent Voice").year(2016).mediaType(TmdbMediaType.MOVIE).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(right));

        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(378064);
        assertThat(result.decision().isConfident()).isTrue();
    }

    @Test
    void spiritedAwayMovieMatches() {
        AniListAnime anime = AniListAnime.builder()
                .id(199).title("Sen to Chihiro no Kamikakushi")
                .englishTitle("Spirited Away").year(2001).format(AnimeFormat.MOVIE).build();
        TmdbTitle right = TmdbTitle.builder()
                .id(129).title("Spirited Away").year(2001).mediaType(TmdbMediaType.MOVIE).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(right));

        assertThat(result.bestCandidate().tmdbTitle().id()).isEqualTo(129);
        assertThat(result.isMatched()).isTrue();
    }

    @Test
    void movieVersusSeriesIsVetoedByFormat() {
        AniListAnime movie = AniListAnime.builder()
                .id(20954).title("Koe no Katachi")
                .englishTitle("A Silent Voice").year(2016).format(AnimeFormat.MOVIE).build();
        TmdbTitle mislabelledAsSeries = TmdbTitle.builder()
                .id(999).title("A Silent Voice").year(2016).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(movie, List.of(mislabelledAsSeries));

        assertThat(result.decision()).isEqualTo(MatchDecision.NO_MATCH);
        assertThat(result.bestCandidate().hardConflict()).isTrue();
    }

    @Test
    void unrelatedCandidatesProduceNoMatch() {
        AniListAnime anime = AniListAnime.builder()
                .id(1).title("Cowboy Bebop").year(1998).format(AnimeFormat.TV).build();
        List<TmdbTitle> candidates = List.of(
                TmdbTitle.builder().id(10).title("Naruto").year(2002).mediaType(TmdbMediaType.TV).build(),
                TmdbTitle.builder().id(11).title("One Piece").year(1999).mediaType(TmdbMediaType.TV).build());

        MatchResult result = matcher.findBestMatch(anime, candidates);

        assertThat(result.decision()).isEqualTo(MatchDecision.NO_MATCH);
        assertThat(result.bestCandidate()).isNotNull();
    }

    @Test
    void nearDuplicateCandidatesAreFlaggedAmbiguous() {
        AniListAnime anime = AniListAnime.builder()
                .id(356).title("Fate/stay night").year(2006).format(AnimeFormat.TV).build();
        TmdbTitle first = TmdbTitle.builder().id(1).title("Fate/stay night").year(2006).mediaType(TmdbMediaType.TV).build();
        TmdbTitle second = TmdbTitle.builder().id(2).title("Fate stay night").year(2006).mediaType(TmdbMediaType.TV).build();

        MatchResult result = matcher.findBestMatch(anime, List.of(first, second));

        assertThat(result.decision()).isEqualTo(MatchDecision.AMBIGUOUS);
        assertThat(result.warnings()).isNotEmpty();
    }
}
