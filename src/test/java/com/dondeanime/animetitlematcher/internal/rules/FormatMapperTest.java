package com.dondeanime.animetitlematcher.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.dondeanime.animetitlematcher.domain.AnimeFormat;
import com.dondeanime.animetitlematcher.domain.TmdbMediaType;
import org.junit.jupiter.api.Test;

class FormatMapperTest {

    @Test
    void matchingTypesScoreOne() {
        assertThat(FormatMapper.score(AnimeFormat.TV, FormatHint.NONE, TmdbMediaType.TV)).hasValue(1.0);
        assertThat(FormatMapper.score(AnimeFormat.MOVIE, FormatHint.NONE, TmdbMediaType.MOVIE)).hasValue(1.0);
    }

    @Test
    void seriesVersusMovieIsHardConflict() {
        assertThat(FormatMapper.score(AnimeFormat.TV, FormatHint.NONE, TmdbMediaType.MOVIE)).hasValue(0.0);
        assertThat(FormatMapper.isHardConflict(AnimeFormat.TV, FormatHint.NONE, TmdbMediaType.MOVIE)).isTrue();
        assertThat(FormatMapper.isHardConflict(AnimeFormat.MOVIE, FormatHint.NONE, TmdbMediaType.TV)).isTrue();
    }

    @Test
    void ovaOnaSpecialAgainstTvIsMildNotConflict() {
        assertThat(FormatMapper.score(AnimeFormat.OVA, FormatHint.NONE, TmdbMediaType.TV)).hasValue(0.70);
        assertThat(FormatMapper.score(AnimeFormat.ONA, FormatHint.NONE, TmdbMediaType.TV)).hasValue(0.70);
        assertThat(FormatMapper.score(AnimeFormat.SPECIAL, FormatHint.NONE, TmdbMediaType.TV)).hasValue(0.70);
        assertThat(FormatMapper.isHardConflict(AnimeFormat.OVA, FormatHint.NONE, TmdbMediaType.TV)).isFalse();
        assertThat(FormatMapper.isHardConflict(AnimeFormat.SPECIAL, FormatHint.NONE, TmdbMediaType.TV)).isFalse();
    }

    @Test
    void ovaAgainstMovieIsMildMismatch() {
        assertThat(FormatMapper.score(AnimeFormat.OVA, FormatHint.NONE, TmdbMediaType.MOVIE)).hasValue(0.45);
        assertThat(FormatMapper.isHardConflict(AnimeFormat.OVA, FormatHint.NONE, TmdbMediaType.MOVIE)).isFalse();
    }

    @Test
    void unknownFormatIsInferredFromTitleHint() {
        assertThat(FormatMapper.score(AnimeFormat.UNKNOWN, FormatHint.MOVIE, TmdbMediaType.MOVIE)).hasValue(1.0);
        assertThat(FormatMapper.isHardConflict(AnimeFormat.UNKNOWN, FormatHint.MOVIE, TmdbMediaType.TV)).isTrue();
    }

    @Test
    void unknownOnEitherSideIsAbsent() {
        assertThat(FormatMapper.score(AnimeFormat.UNKNOWN, FormatHint.NONE, TmdbMediaType.TV)).isEmpty();
        assertThat(FormatMapper.score(AnimeFormat.TV, FormatHint.NONE, TmdbMediaType.UNKNOWN)).isEmpty();
    }
}
