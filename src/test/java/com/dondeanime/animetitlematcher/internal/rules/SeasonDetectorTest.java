package com.dondeanime.animetitlematcher.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SeasonDetectorTest {

    @Test
    void detectsSeasonAndPartTogether() {
        TitleSequence sequence = SeasonDetector.detect("shingeki no kyojin season 3 part 2");
        assertThat(sequence.season()).isEqualTo(3);
        assertThat(sequence.part()).isEqualTo(2);
        assertThat(sequence.baseTitle()).isEqualTo("shingeki no kyojin");
    }

    @Test
    void detectsOrdinalSeason() {
        TitleSequence sequence = SeasonDetector.detect("boku no hero academia 2nd season");
        assertThat(sequence.season()).isEqualTo(2);
        assertThat(sequence.baseTitle()).isEqualTo("boku no hero academia");
    }

    @Test
    void detectsSeasonKeywordForm() {
        TitleSequence sequence = SeasonDetector.detect("jujutsu kaisen season 2");
        assertThat(sequence.season()).isEqualTo(2);
        assertThat(sequence.baseTitle()).isEqualTo("jujutsu kaisen");
    }

    @Test
    void detectsSpelledOutSeasonAndPart() {
        assertThat(SeasonDetector.detect("second season").season()).isEqualTo(2);
        assertThat(SeasonDetector.detect("clannad part two").part()).isEqualTo(2);
    }

    @Test
    void detectsCour() {
        TitleSequence sequence = SeasonDetector.detect("some title cour 2");
        assertThat(sequence.cour()).isEqualTo(2);
        assertThat(sequence.baseTitle()).isEqualTo("some title");
    }

    @Test
    void detectsFinalSeasonMarker() {
        TitleSequence sequence = SeasonDetector.detect("attack on titan final season");
        assertThat(sequence.finalMarker()).isTrue();
        assertThat(sequence.season()).isNull();
        assertThat(sequence.baseTitle()).isEqualTo("attack on titan");
    }

    @Test
    void detectsTrailingFormatHints() {
        assertThat(SeasonDetector.detect("kara no kyoukai the movie").formatHint()).isEqualTo(FormatHint.MOVIE);
        assertThat(SeasonDetector.detect("kara no kyoukai the movie").baseTitle()).isEqualTo("kara no kyoukai");
        assertThat(SeasonDetector.detect("some title ova").formatHint()).isEqualTo(FormatHint.OVA);
        assertThat(SeasonDetector.detect("some title special").formatHint()).isEqualTo(FormatHint.SPECIAL);
    }

    @Test
    void preservesLeadingWordsThatLookLikeFormatHints() {
        TitleSequence sequence = SeasonDetector.detect("special a");
        assertThat(sequence.formatHint()).isEqualTo(FormatHint.NONE);
        assertThat(sequence.baseTitle()).isEqualTo("special a");
    }

    @Test
    void leavesBareTrailingNumberInTitle() {
        TitleSequence sequence = SeasonDetector.detect("macross 2");
        assertThat(sequence.hasSequenceInfo()).isFalse();
        assertThat(sequence.baseTitle()).isEqualTo("macross 2");
    }

    @Test
    void titleWithoutMarkersHasNoSequenceInfo() {
        TitleSequence sequence = SeasonDetector.detect("naruto");
        assertThat(sequence.hasSequenceInfo()).isFalse();
        assertThat(sequence.baseTitle()).isEqualTo("naruto");
    }

    @Test
    void handlesNullAndBlank() {
        assertThat(SeasonDetector.detect(null).hasSequenceInfo()).isFalse();
        assertThat(SeasonDetector.detect(null).baseTitle()).isEmpty();
        assertThat(SeasonDetector.detect("   ").baseTitle()).isEmpty();
    }
}
