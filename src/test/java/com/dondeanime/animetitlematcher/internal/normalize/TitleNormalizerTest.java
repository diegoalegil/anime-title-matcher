package com.dondeanime.animetitlematcher.internal.normalize;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TitleNormalizerTest {

    private final TitleNormalizer normalizer = new TitleNormalizer();

    @Test
    void handlesNullAndBlank() {
        assertThat(normalizer.normalize(null).isEmpty()).isTrue();
        assertThat(normalizer.normalize(null).original()).isEmpty();
        assertThat(normalizer.normalize("   ").isEmpty()).isTrue();
    }

    @Test
    void preservesTheOriginalTitle() {
        assertThat(normalizer.normalize("Pokémon").original()).isEqualTo("Pokémon");
    }

    @Test
    void lowercasesStripsAccentsAndSymbols() {
        assertThat(normalizer.normalize("Pokémon").normalized()).isEqualTo("pokemon");
        assertThat(normalizer.normalize("Fate/stay night: Unlimited Blade Works").normalized())
                .isEqualTo("fate stay night unlimited blade works");
        assertThat(normalizer.normalize("Steins;Gate").normalized()).isEqualTo("steins gate");
        assertThat(normalizer.normalize("Re:Zero kara Hajimeru Isekai Seikatsu").normalized())
                .isEqualTo("re zero kara hajimeru isekai seikatsu");
        assertThat(normalizer.normalize("Kaguya-sama wa Kokurasetai: Ultra Romantic").normalized())
                .isEqualTo("kaguya sama wa kokurasetai ultra romantic");
    }

    @Test
    void expandsAmpersand() {
        assertThat(normalizer.normalize("Tom & Jerry").normalized()).isEqualTo("tom and jerry");
    }

    @Test
    void convertsRomanNumerals() {
        assertThat(normalizer.normalize("Macross II").normalized()).isEqualTo("macross 2");
    }

    @Test
    void removesStopwords() {
        assertThat(normalizer.normalize("The Promised Neverland").normalized()).isEqualTo("promised neverland");
    }

    @Test
    void extractsSeasonAndPartAsMetadataAndStripsThemFromTheBaseTitle() {
        NormalizedTitle normalized = normalizer.normalize("Shingeki no Kyojin Season 3 Part 2");
        assertThat(normalized.normalized()).isEqualTo("shingeki no kyojin");
        assertThat(normalized.sequence().season()).isEqualTo(3);
        assertThat(normalized.sequence().part()).isEqualTo(2);
    }

    @Test
    void extractsOrdinalSeason() {
        NormalizedTitle normalized = normalizer.normalize("Boku no Hero Academia 2nd Season");
        assertThat(normalized.normalized()).isEqualTo("boku no hero academia");
        assertThat(normalized.sequence().season()).isEqualTo(2);
    }

    @Test
    void buildsTokenListAndSet() {
        NormalizedTitle normalized = normalizer.normalize("Attack on Titan");
        assertThat(normalized.tokens()).containsExactly("attack", "on", "titan");
        assertThat(normalized.tokenSet()).containsExactlyInAnyOrder("attack", "on", "titan");
    }

    @Test
    void keepsNonLatinScriptsIntact() {
        assertThat(normalizer.normalize("進撃の巨人").normalized()).isEqualTo("進撃の巨人");
    }

    @Test
    void optionsCanDisableStopwordRemoval() {
        TitleNormalizer noStopwords = new TitleNormalizer(new TitleNormalizationOptions(
                true, true, true, true, true, true, true, false));
        assertThat(noStopwords.normalize("The Promised Neverland").normalized())
                .isEqualTo("the promised neverland");
    }

    @Test
    void neverProducesEmptyTokensWhenOnlyStopwordsRemain() {
        // "Vol" alone is a stopword; stripping it would empty the title, so it is kept.
        assertThat(normalizer.normalize("Vol").isEmpty()).isFalse();
    }
}
