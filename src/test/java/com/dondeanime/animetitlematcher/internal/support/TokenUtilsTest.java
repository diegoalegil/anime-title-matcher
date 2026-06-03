package com.dondeanime.animetitlematcher.internal.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenUtilsTest {

    @Test
    void collapseWhitespaceTrimsAndCollapses() {
        assertThat(TokenUtils.collapseWhitespace("  attack   on  titan ")).isEqualTo("attack on titan");
    }

    @Test
    void collapseWhitespaceHandlesNullAndBlank() {
        assertThat(TokenUtils.collapseWhitespace(null)).isEmpty();
        assertThat(TokenUtils.collapseWhitespace("   ")).isEmpty();
    }

    @Test
    void tokenizeSplitsOnWhitespace() {
        assertThat(TokenUtils.tokenize("attack on  titan")).containsExactly("attack", "on", "titan");
    }

    @Test
    void tokenizeHandlesNullAndBlank() {
        assertThat(TokenUtils.tokenize(null)).isEmpty();
        assertThat(TokenUtils.tokenize("   ")).isEmpty();
    }

    @Test
    void tokenizeSingleToken() {
        assertThat(TokenUtils.tokenize("naruto")).containsExactly("naruto");
    }

    @Test
    void joinReinsertsSingleSpaces() {
        assertThat(TokenUtils.join(java.util.List.of("attack", "on", "titan"))).isEqualTo("attack on titan");
    }
}
