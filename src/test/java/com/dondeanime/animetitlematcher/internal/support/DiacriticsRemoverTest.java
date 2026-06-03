package com.dondeanime.animetitlematcher.internal.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DiacriticsRemoverTest {

    @Test
    void returnsNullForNull() {
        assertThat(DiacriticsRemover.removeDiacritics(null)).isNull();
    }

    @Test
    void returnsEmptyForEmpty() {
        assertThat(DiacriticsRemover.removeDiacritics("")).isEmpty();
    }

    @Test
    void removesCommonLatinAccents() {
        assertThat(DiacriticsRemover.removeDiacritics("café")).isEqualTo("cafe");
        assertThat(DiacriticsRemover.removeDiacritics("Pokémon")).isEqualTo("Pokemon");
        assertThat(DiacriticsRemover.removeDiacritics("naïve")).isEqualTo("naive");
    }

    @Test
    void removesMacronsFromRomanisedJapanese() {
        assertThat(DiacriticsRemover.removeDiacritics("Tōkyō")).isEqualTo("Tokyo");
        assertThat(DiacriticsRemover.removeDiacritics("Yūki")).isEqualTo("Yuki");
    }

    @Test
    void leavesUnaccentedTextUnchanged() {
        assertThat(DiacriticsRemover.removeDiacritics("Fullmetal Alchemist")).isEqualTo("Fullmetal Alchemist");
    }

    @Test
    void leavesNonLatinScriptsUnchanged() {
        assertThat(DiacriticsRemover.removeDiacritics("進撃の巨人")).isEqualTo("進撃の巨人");
    }
}
