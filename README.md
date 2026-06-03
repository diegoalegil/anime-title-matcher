<div align="center">

# 🎬 anime-title-matcher

**Match an AniList anime to its most probable TMDb entry — fuzzily, and explainably.**

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![JitPack](https://jitpack.io/v/diegoalegil/anime-title-matcher.svg)](https://jitpack.io/#diegoalegil/anime-title-matcher)
[![Build](https://img.shields.io/badge/build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/tests-147%20passing-2ea44f)](#running-the-tests)
[![Runtime deps](https://img.shields.io/badge/runtime%20dependencies-0-2ea44f)](#stack)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

</div>

A small, dependency-free Java library that, given an anime from **AniList** and a list of candidate
entries from **TMDb**, decides which candidate is the most probable equivalent — using title
normalization, hand-written fuzzy string similarity, and an explainable, multi-signal scoring model.

It never returns a bare boolean or a lone number. Every match comes with a **decision**, a
**score** in `[0.0, 1.0]`, a full per-signal **breakdown**, and a human-readable **explanation** of
*why* that candidate won — or why nothing did.

## Table of contents

- [Why](#why) · [DondeAnime context](#dondeanime-context) · [Stack](#stack)
- [Install](#install) · [Quick start](#quick-start) · [What you get back](#what-you-get-back)
- [Interpreting a result](#interpreting-a-matchresult) · [Decision thresholds](#decision-thresholds)
- [How it works](#how-it-works) · [Hard cases](#hard-cases-supported) · [Project layout](#project-layout)
- [Tests](#running-the-tests) · [Adding cases](#adding-evaluation-cases)
- [Limitations](#limitations) · [Roadmap](#roadmap) · [Design notes](#technical-decisions) · [License](#license)

## Why

Cross-referencing AniList and TMDb is hard: the same show appears under different titles, seasons,
subtitles, languages, punctuation and formats. This library targets exactly those hard cases and
makes every decision **testable and explainable**, so it can be reused and trusted.

✨ **At a glance**

- 🔤 **Robust normalization** — accents, punctuation, roman numerals, seasons, stopwords.
- 🧮 **Hand-written similarity** — Levenshtein, Jaro-Winkler and token-set, no external library.
- ⚖️ **Multi-signal scoring** — title, season, year, format and episodes, renormalized over what's known.
- 🛡️ **Conflict caps, not double penalties** — a TV-vs-movie or wrong-year clash is counted once.
- 🔎 **Explainable** — a breakdown and a sentence for every decision; deterministic ordering.
- 🌐 **Offline & pure** — no network, no API keys, **zero runtime dependencies**.

## DondeAnime context

This project was extracted from **DondeAnime**, an anime "where to watch" aggregator. DondeAnime
currently cross-references AniList against TMDb and correctly matches **767 of 929 titles (~83%)**.
The remaining ~17% fail on seasons, roman numerals, subtitles, Japanese-vs-international titles,
punctuation, accents, movie-vs-series confusion, year drift, episode-count drift and alternative
titles. This library is a robust, well-tested implementation of that matching concern.

> **Honest baseline.** The 767/929 (~83%) figure is DondeAnime's *current* baseline, measured on its
> full private dataset. This repository does **not** claim to beat it — the real improvement can only
> be measured inside DondeAnime against the complete dataset. The bundled sample here is a small,
> representative fixture used to validate behaviour offline. See [docs/evaluation.md](docs/evaluation.md).

## Stack

| | |
| --- | --- |
| Language / build | **Java 21**, **Maven** (with wrapper) |
| Tests | **JUnit 5** + **AssertJ**, **JaCoCo** coverage |
| Runtime dependencies | **none** — similarity algorithms implemented by hand |
| Not used | Spring, Lombok, external string-similarity / fuzzy libraries |

## Install

Available from [JitPack](https://jitpack.io/#diegoalegil/anime-title-matcher) — add the repository
and the dependency:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.diegoalegil</groupId>
  <artifactId>anime-title-matcher</artifactId>
  <version>v0.1.0</version>
</dependency>
```

Or build and install it locally (`com.dondeanime:anime-title-matcher:0.1.0`):

```bash
./mvnw clean install
```

## Quick start

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
    System.out.println(result.bestCandidate().tmdbTitle().title());
    System.out.println(result.decision());          // e.g. HIGH_CONFIDENCE
    System.out.println(result.explanation().summary());
}
```

## What you get back

A `MatchResult` is fully inspectable (values below are illustrative):

```text
decision      HIGH_CONFIDENCE
score         0.94
best match    TMDb "Demon Slayer: Kimetsu no Yaiba" (id 85937)
matched       "Demon Slayer: Kimetsu no Yaiba …"  ⟶  "Demon Slayer: Kimetsu no Yaiba"
signals used  [title, year, format]
breakdown     title 1.00 · altTitle 0.92 · season – · year 1.00 · format 1.00 · episodes – · penalty 0.00
explanation   HIGH_CONFIDENCE: matched "…Entertainment District Arc" to
              TMDb "Demon Slayer: Kimetsu no Yaiba" (id 85937) at 0.94.
```

Inspect or re-rank the field with `allCandidates()` (sorted by score, descending):

```java
for (MatchCandidate c : result.allCandidates()) {
    System.out.printf("%6.3f  %s%n", c.score(), c.tmdbTitle().title());
}
if (result.decision() == MatchDecision.AMBIGUOUS) {
    result.warnings().forEach(System.out::println);
}
```

## Interpreting a `MatchResult`

| Member | Meaning |
| --- | --- |
| `decision()` | `EXACT_MATCH`, `HIGH_CONFIDENCE`, `MEDIUM_CONFIDENCE`, `LOW_CONFIDENCE`, `AMBIGUOUS` or `NO_MATCH` |
| `bestCandidate()` | top-scoring candidate (the closest one is reported even on `NO_MATCH`; `null` only when no candidates were given) |
| `score()` | the best candidate's final score in `[0.0, 1.0]` |
| `scoreBreakdown()` | the per-signal account behind the score |
| `allCandidates()` | every candidate, sorted by score descending |
| `warnings()` | non-fatal notes, e.g. an ambiguity warning |
| `explanation()` | human-readable summary, matched titles, signals used and any rejection reason |

A `ScoreBreakdown` exposes `titleScore`, `alternativeTitleScore`, `seasonScore`, `yearScore`,
`formatScore`, `episodeScore`, `penaltyScore` and `finalScore`. Signals that did not apply are
`null` rather than `0.0`, so "absent" is distinguishable from "scored zero".

## Decision thresholds

With the default `MatchThresholds`:

| Decision | Default condition |
| --- | --- |
| `EXACT_MATCH` | titles identical after normalization, score ≥ 0.95, no hard conflict |
| `HIGH_CONFIDENCE` | score ≥ 0.85 |
| `MEDIUM_CONFIDENCE` | score ≥ 0.70 |
| `LOW_CONFIDENCE` | score ≥ 0.55 |
| `NO_MATCH` | score below 0.55 |
| `AMBIGUOUS` | best candidate within 0.05 of the runner-up (both ≥ 0.55) |

```java
AnimeTitleMatcher matcher = AnimeTitleMatcher.create(
    new ScoringWeights(0.60, 0.10, 0.12, 0.10, 0.08),   // title, season, year, format, episodes
    MatchThresholds.defaults()
);
```

## How it works

**1 · Normalize** ([details](docs/normalization.md)) — lower-case, strip accents, drop bracketed
noise, expand `&`, delete apostrophes, symbols → spaces (kana/kanji preserved), roman numerals →
arabic, then extract season/part/cour and format markers as **metadata** (never silently dropped),
and remove a few stopwords. The original title is always kept.

```
"Shingeki no Kyojin Season 3 Part 2"  ⟶  "shingeki no kyojin"   (+ season 3, part 2)
"Fate/stay night: Unlimited Blade Works"  ⟶  "fate stay night unlimited blade works"
```

**2 · Compare** ([details](docs/scoring.md)) — title similarity blends three hand-written measures,
each in `[0,1]`: **Levenshtein** (two-row DP, `O(n·m)` time / `O(min(n,m))` space), **Jaro-Winkler**,
and **token-set** overlap (Jaccard + token-sort) so Japanese↔English word reordering still scores high.

**3 · Score** — a weighted average of the *present* signals (title, season, year, format, episodes),
renormalized so missing data never drags a candidate down. Hard conflicts are enforced as score
**caps**, not extra penalties:

- **TV vs movie** is the only hard format conflict (OVA/ONA/SPECIAL against TMDb `tv` is a mild
  positive — TMDb files them under `tv`).
- A **large year gap** caps the score, so a wrong-year match can't be high confidence — this is what
  separates *Fullmetal Alchemist* (2003) from *Brotherhood* (2009).

## Hard cases supported

Seasons (`Season 3 Part 2`, `2nd Season`, `Final Season`, `Cour 2`) · roman numerals (`II` → `2`) ·
subtitles · Japanese vs international titles · alternative/synonym titles · punctuation
(`Steins;Gate`, `Fate/stay night`, `Re:Zero`) · accents (`Pokémon`, `Tōkyō`) · movie-vs-series
vetoes · year disambiguation · episode-count drift.

## Project layout

```
src/main/java/com/dondeanime/animetitlematcher/
├─ api/        AnimeTitleMatcher · MatchRequest/Result/Candidate/Decision/Explanation
│              ScoreBreakdown · ScoringWeights · MatchThresholds       (public, stable)
├─ domain/     AniListAnime · TmdbTitle · AnimeFormat · TmdbMediaType · TitleVariant
└─ internal/   normalize · similarity · score · rules · support        (implementation detail)
```

Everything under `internal` is an implementation detail and not part of the stable API.

## Running the tests

```bash
./mvnw test          # unit + end-to-end tests
./mvnw clean verify  # full build, tests and JaCoCo report (target/site/jacoco)
```

Tests never touch the network and do not require TMDb or AniList access.

## Adding evaluation cases

Append cases to
[`src/test/resources/fixtures/matching-cases.json`](src/test/resources/fixtures/matching-cases.json) —
each provides an `anilist` object, `candidates`, an `expectedTmdbId` (or `null`) and `expectMatch`.
`MatchingEvaluator` then reports `total / correct / incorrect / noMatch / ambiguous / accuracy`. See
[docs/evaluation.md](docs/evaluation.md).

## Limitations

- The evaluator's accuracy reflects only the **small, curated sample** in this repository, not the
  full DondeAnime dataset. It validates behaviour; it is not a benchmark.
- No TMDb/AniList HTTP clients are shipped — the library works purely on the objects you pass in, so
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

- **No external similarity library** — Levenshtein and Jaro-Winkler are implemented by hand.
- **Immutable models** (records + builders) and a small, stable public API; internals are clearly separated.
- **Explainability first** — every result carries a breakdown and an explanation; scoring uses caps
  instead of stacked penalties so it stays easy to reason about.
- **Deterministic** — candidate ordering uses an explicit score → id → input-order comparator.

## License

Released under the [MIT License](LICENSE).
