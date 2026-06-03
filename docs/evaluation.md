# Evaluation

The matcher can be measured against a local dataset entirely offline — no TMDb or AniList access is
required.

## Fixtures

Cases live in [`src/test/resources/fixtures/matching-cases.json`](../src/test/resources/fixtures/matching-cases.json).
Each case supplies an AniList anime, the candidates to choose from, the expected TMDb id (or `null`),
and whether a match is expected:

```json
{
  "name": "Demon Slayer (native + english)",
  "anilist": {
    "id": 101922,
    "romajiTitle": "Kimetsu no Yaiba",
    "englishTitle": "Demon Slayer: Kimetsu no Yaiba",
    "nativeTitle": "鬼滅の刃",
    "year": 2019,
    "format": "TV"
  },
  "candidates": [
    { "id": 85937, "title": "Demon Slayer: Kimetsu no Yaiba", "originalTitle": "鬼滅の刃", "year": 2019, "mediaType": "tv" },
    { "id": 31910, "title": "Naruto: Shippuden", "year": 2007, "mediaType": "tv" }
  ],
  "expectedTmdbId": 85937,
  "expectMatch": true
}
```

`format` accepts AniList values (`TV`, `MOVIE`, `OVA`, `ONA`, `SPECIAL`, …) and `mediaType` accepts
TMDb values (`tv`, `movie`).

## Running the evaluator

```java
List<EvaluationCase> cases = FixtureLoader.loadCases("/fixtures/matching-cases.json");
MatchingEvaluator.Metrics metrics =
    new MatchingEvaluator(AnimeTitleMatcher.createDefault()).evaluate(cases);

System.out.println(metrics); // total, correct, incorrect, noMatch, ambiguous, accuracy
```

`MatchingEvaluator` reports:

| Field | Meaning |
| --- | --- |
| `total` | number of cases |
| `correct` | resolved exactly as expected (right candidate, or a correct no-match) |
| `incorrect` | matched to the wrong candidate, or a false positive on a no-match case |
| `noMatch` | expected a match but produced `NO_MATCH` |
| `ambiguous` | expected a match but was flagged `AMBIGUOUS` |
| `accuracy` | `correct / total` |

The test `MatchingEvaluatorTest` runs this over the bundled sample and asserts a high accuracy floor.

## What the numbers mean (and don't)

The bundled dataset is a **small, curated, representative sample**, chosen to exercise the hard
cases. The matcher resolves all of it today, but that is a sanity check on behaviour, **not** a
benchmark.

DondeAnime's production baseline is **767 of 929 (~83%)**, measured on its full private dataset. The
real effect of this library can only be measured by running it against that complete dataset inside
DondeAnime. This repository deliberately makes no claim about beating 83% without that data.

## Adding cases

Append objects to the `cases` array in `matching-cases.json` and re-run `./mvnw test`. To capture a
real production miss, copy the AniList fields and the TMDb candidates that were considered, set the
`expectedTmdbId` to the correct entry, and the evaluator will track whether the matcher gets it right.
