# anime-title-matcher

![Java](https://img.shields.io/badge/Java-21-blue) ![Build](https://img.shields.io/badge/build-Maven-orange) ![License](https://img.shields.io/badge/license-MIT-green)

A small, dependency-free Java library that, given an anime from **AniList** and a list of candidate
entries from **TMDb**, decides which candidate is the most probable equivalent — using title
normalization, hand-written fuzzy string similarity and an explainable, multi-signal scoring model.

The library does not return a bare boolean or a single number. For every match it tells you the
chosen candidate, a score in `[0.0, 1.0]`, a confidence **decision**, a full per-signal
**breakdown**, and a human-readable **explanation** of why that candidate won (or why nothing did).

## Objective

Cross-referencing AniList and TMDb is hard because the same show appears under different titles,
seasons, subtitles, languages, punctuation and formats. This library focuses on exactly those hard
cases and makes every decision **testable and explainable**, so it can be reused and trusted.

## DondeAnime context

This project was extracted from **DondeAnime**, an anime "where to watch" aggregator. DondeAnime
currently cross-references AniList against TMDb and correctly matches **767 of 929 titles (~83%)**.
The remaining ~17% fail on seasons, roman numerals, subtitles, Japanese-vs-international titles,
punctuation, accents, movie-vs-series confusion, year drift, episode-count drift and alternative
titles. This library is a robust, well-tested implementation of that matching concern, designed to
improve on those hard cases and to be reusable as a standalone dependency.

> The 767/929 (~83%) figure is DondeAnime's **current baseline**, measured on its full private
> dataset. This repository does **not** claim to beat it; the real improvement can only be measured
> inside DondeAnime against the complete dataset. The bundled sample here is a small, representative
> fixture used to validate behaviour offline. See [docs/evaluation.md](docs/evaluation.md).

## Stack

- **Java 21**, **Maven**
- **JUnit 5** + **AssertJ** for tests
- **Zero runtime dependencies** — Levenshtein and Jaro-Winkler are implemented by hand
- No Spring, no Lombok, no external string-similarity library

## Installation

Build and install locally:

```bash
./mvnw clean install
```

Then depend on it:

```xml
<dependency>
    <groupId>com.dondeanime</groupId>
    <artifactId>anime-title-matcher</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Basic usage

```java
import com.dondeanime.animetitlematcher.api.*;
import com.dondeanime.animetitlematcher.domain.*;
import java.util.List;

AnimeTitleMatcher matcher = AnimeTitleMatcher.createDefault();

MatchResult result = matcher.findBestMatch(
    AniListAnime.builder()
        .id(1L)
        .title("Kimetsu no Yaiba: Yuukaku-hen")
        .englishTitle("Demon Slayer: Kimetsu no Yaiba Entertainment District Arc")
        .year(2021)
        .format(AnimeFormat.TV)
        .episodes(11)
        .build(),
    List.of(
        TmdbTitle.builder()
            .id(85937L)
            .title("Demon Slayer: Kimetsu no Yaiba")
            .originalTitle("鬼滅の刃")
            .year(2019)
            .mediaType(TmdbMediaType.TV)
            .build(),
        new TmdbTitle(1429L, "Attack on Titan", "進撃の巨人", 2013, TmdbMediaType.TV, 88)
    )
);

if (result.isMatched()) {
    MatchCandidate best = result.bestCandidate();
    System.out.println(best.tmdbTitle().title());   // chosen TMDb title
    System.out.println(result.decision());          // e.g. HIGH_CONFIDENCE
    System.out.println(result.score());             // 0.0 .. 1.0
    System.out.println(result.explanation().summary());
}
```

## Working with several candidates

`allCandidates()` returns every candidate scored and sorted by score, descending, so you can inspect
the runner-up or apply your own policy:

```java
MatchResult result = matcher.findBestMatch(anime, candidates);

for (MatchCandidate candidate : result.allCandidates()) {
    System.out.printf("%6.3f  %s%n", candidate.score(), candidate.tmdbTitle().title());
}

if (result.decision() == MatchDecision.AMBIGUOUS) {
    result.warnings().forEach(System.out::println);
}
```

## Interpreting a `MatchResult`

| Member | Meaning |
| --- | --- |
| `decision()` | `EXACT_MATCH`, `HIGH_CONFIDENCE`, `MEDIUM_CONFIDENCE`, `LOW_CONFIDENCE`, `AMBIGUOUS` or `NO_MATCH` |
| `bestCandidate()` | the top-scoring candidate (the closest one is reported even on `NO_MATCH`; `null` only when no candidates were given) |
| `score()` | the best candidate's final score in `[0.0, 1.0]` |
| `scoreBreakdown()` | the per-signal account behind the score |
| `allCandidates()` | every candidate, sorted by score descending |
| `warnings()` | non-fatal notes, e.g. an ambiguity warning |
| `explanation()` | human-readable summary, matched titles, signals used and any rejection reason |

A `ScoreBreakdown` exposes `titleScore`, `alternativeTitleScore`, `seasonScore`, `yearScore`,
`formatScore`, `episodeScore`, `penaltyScore` and `finalScore`. Signals that did not apply (for
example a year comparison when a year is unknown) are `null` rather than `0.0`, so "absent" is
distinguishable from "scored zero".

## How normalization works

Each title is run through a configurable pipeline that lower-cases, strips accents, removes bracketed
noise, expands `&`, deletes apostrophes, turns other symbols into spaces, converts roman numerals,
extracts season/part/cour and format markers as **metadata**, and removes a few low-value stopwords.
The original title is always kept. Crucially, season and format markers are **not discarded** — they
are parsed into structured metadata and reused by the scorer. Details and examples are in
[docs/normalization.md](docs/normalization.md).

## How similarity works

Title similarity blends three hand-written measures, all returning `[0.0, 1.0]`:

- **Levenshtein** edit distance (two-row DP, `O(n·m)` time / `O(min(n,m))` space), normalized.
- **Jaro-Winkler**, which rewards matching characters and shared prefixes.
- **Token-set** overlap (Jaccard and token-sort), so word reordering between Japanese and English
  titles still scores high.

These are combined by `CompositeTitleSimilarity`. See [docs/scoring.md](docs/scoring.md) for the
exact blend.

## How scoring works

The final score is a weighted average of the present signals (title, season, year, format,
episodes), renormalized so that missing data never drags a candidate down. Hard conflicts are
enforced as score **caps**, not extra penalties, so a conflict is counted exactly once:

- **TV vs movie** is the only hard format conflict (OVA/ONA/SPECIAL against TMDb `tv` is treated as a
  mild positive, since TMDb files them under `tv`).
- A **large year gap** caps the score so a wrong-year match cannot be reported as high confidence.

Weights and thresholds are configurable via `ScoringWeights` and `MatchThresholds`. The full model,
including default weights, is documented in [docs/scoring.md](docs/scoring.md).

## Decision thresholds

With the default `MatchThresholds`:

| Decision | Condition |
| --- | --- |
| `EXACT_MATCH` | titles identical after normalization, score ≥ 0.95, and no hard conflict |
| `HIGH_CONFIDENCE` | score ≥ 0.85 |
| `MEDIUM_CONFIDENCE` | score ≥ 0.70 |
| `LOW_CONFIDENCE` | score ≥ 0.55 |
| `NO_MATCH` | score below 0.55 |
| `AMBIGUOUS` | the best candidate is within 0.05 of the runner-up (and both clear the low threshold) |

Customize them:

```java
AnimeTitleMatcher matcher = AnimeTitleMatcher.create(
    new ScoringWeights(0.60, 0.10, 0.12, 0.10, 0.08),
    MatchThresholds.defaults()
);
```

## Hard cases supported

Seasons (`Season 3 Part 2`, `2nd Season`, `Final Season`, `Cour 2`), roman numerals (`II` → `2`),
subtitles, Japanese vs international titles, alternative/synonym titles, punctuation
(`Steins;Gate`, `Fate/stay night`, `Re:Zero`), accents (`Pokémon`, `Tōkyō`), movie-vs-series
vetoes, year disambiguation (e.g. *Fullmetal Alchemist* 2003 vs *Brotherhood* 2009) and
episode-count drift.

## Running the tests

```bash
./mvnw test          # unit and end-to-end tests
./mvnw clean verify  # full build, tests and JaCoCo coverage report (target/site/jacoco)
```

Tests never touch the network and do not require TMDb or AniList access.

## Adding new evaluation cases

Append cases to [`src/test/resources/fixtures/matching-cases.json`](src/test/resources/fixtures/matching-cases.json).
Each case provides an `anilist` object, a list of `candidates`, the `expectedTmdbId` (or `null`),
and `expectMatch`. The `MatchingEvaluator` then reports accuracy over the whole set. See
[docs/evaluation.md](docs/evaluation.md).

## Limitations

- The accuracy reported by the bundled evaluator reflects only the **small, curated sample** in this
  repository, not the full DondeAnime dataset. It validates behaviour; it is not a benchmark.
- No TMDb/AniList HTTP clients are shipped. The library works purely on the objects you pass in, so
  it stays offline, fast and testable.
- Native (Japanese) titles only contribute when both sides carry comparable text (e.g. a TMDb
  `original_title` in Japanese).

## Roadmap

- More real fixtures and weight tuning against a larger dataset.
- Optional, pluggable TMDb/AniList provider interfaces.
- Better handling of Japanese titles and season detection.
- An exportable evaluation report and an optional CLI for trying datasets.
- Publishing to a Maven repository.

## Technical decisions

- **No external similarity library.** Levenshtein and Jaro-Winkler are implemented by hand so the
  project demonstrates the underlying algorithms.
- **Immutable models** (records with builders) and a small, stable public API under
  `com.dondeanime.animetitlematcher.api` and `.domain`; everything under `.internal` is an
  implementation detail.
- **Explainability first.** Every result carries a breakdown and an explanation; scoring uses caps
  instead of stacked penalties so it stays easy to reason about.
- **Deterministic.** Candidate ordering uses an explicit score → id → input-order comparator.

## License

Released under the [MIT License](LICENSE).
