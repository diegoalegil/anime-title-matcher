package com.dondeanime.animetitlematcher.internal.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextSanitizerTest {

    private static String pipeline(String input) {
        String cleaned = TextSanitizer.removeParentheticals(input);
        cleaned = TextSanitizer.expandAmpersand(cleaned);
        cleaned = TextSanitizer.removeApostrophes(cleaned);
        cleaned = TextSanitizer.stripSymbols(cleaned);
        return TokenUtils.collapseWhitespace(cleaned);
    }

    @Test
    void removesBracketedSegments() {
        assertThat(pipeline("Naruto (TV)")).isEqualTo("Naruto");
        assertThat(pipeline("Clannad [2007]")).isEqualTo("Clannad");
    }

    @Test
    void expandsAmpersandToWord() {
        assertThat(pipeline("Tom & Jerry")).isEqualTo("Tom and Jerry");
    }

    @Test
    void deletesApostrophesWithoutSplittingWords() {
        assertThat(pipeline("Journey's End")).isEqualTo("Journeys End");
        assertThat(pipeline("Frieren: Beyond Journey’s End")).isEqualTo("Frieren Beyond Journeys End");
    }

    @Test
    void turnsSeparatorsIntoSpaces() {
        assertThat(pipeline("Fate/stay night")).isEqualTo("Fate stay night");
        assertThat(pipeline("Steins;Gate")).isEqualTo("Steins Gate");
        assertThat(pipeline("Re:Zero")).isEqualTo("Re Zero");
        assertThat(pipeline("Kaguya-sama")).isEqualTo("Kaguya sama");
    }

    @Test
    void stripSymbolsKeepsLettersDigitsAndNonLatinScripts() {
        assertThat(TextSanitizer.stripSymbols("steins;gate!").trim()).isEqualTo("steins gate");
        assertThat(TokenUtils.collapseWhitespace(TextSanitizer.stripSymbols("進撃の巨人"))).isEqualTo("進撃の巨人");
    }

    @Test
    void allMethodsAreNullSafe() {
        assertThat(TextSanitizer.removeParentheticals(null)).isEmpty();
        assertThat(TextSanitizer.expandAmpersand(null)).isEmpty();
        assertThat(TextSanitizer.removeApostrophes(null)).isEmpty();
        assertThat(TextSanitizer.stripSymbols(null)).isEmpty();
    }
}
