# Scoring

A candidate's final score is built from several signals, each in `[0.0, 1.0]`, combined into a
weighted average and then bounded by hard-conflict caps.

## Title similarity

`CompositeTitleSimilarity` blends three hand-written measures. To handle word reordering between
Japanese and English titles, the character-level measures are taken over **both** the strings as-is
and their token-sorted forms, keeping the better of the two:

```
jaroWinkler = max( JW(a, b), JW(sort(a), sort(b)) )
levenshtein = max( Lev(a, b), Lev(sort(a), sort(b)) )
jaccard     = |tokens(a) ∩ tokens(b)| / |tokens(a) ∪ tokens(b)|

titleSimilarity = 0.45·jaroWinkler + 0.30·levenshtein + 0.25·jaccard
```

The **Levenshtein** distance uses a two-row dynamic-programming table: `O(n·m)` time and
`O(min(n, m))` space, where `n` and `m` are the string lengths. Identical strings (and two empty
strings) score `1.0`; a non-empty string against an empty one scores `0.0`.

The matcher compares every AniList title variant against every TMDb variant and keeps the best.
Matches that involve a synonym or alternative title are discounted by a small factor (`0.97`) so a
canonical-title match wins ties. The breakdown reports `titleScore` (best among canonical titles)
and `alternativeTitleScore` (best involving an alternative) separately.

## Signals and default weights

| Signal | What it measures | Default weight | Absent when |
| --- | --- | --- | --- |
| title | best composite title similarity | 0.55 | never (always present) |
| season | season/part/cour agreement | 0.10 | only one side carries sequence info |
| year | release-year proximity | 0.13 | either year unknown |
| format | format agreement | 0.12 | either format unknown |
| episodes | episode-count proximity | 0.10 | either count unknown/non-positive |

Weights need not sum to `1.0`: they are **renormalized over the signals that are actually present**,
so a missing year or episode count never drags a candidate down.

```
raw = Σ (weightᵢ · scoreᵢ)  /  Σ weightᵢ      (over present signals)
```

## Hard conflicts as caps (not extra penalties)

Two situations are strong evidence of a wrong match. They are enforced as score **caps**, and the
conflicting signal is dropped from the weighted average, so a conflict is counted exactly once and is
never double-penalised:

- **TV vs movie** — the only hard format conflict. Caps the score at `0.45` (below the low
  threshold). OVA/ONA/SPECIAL against TMDb `tv` is **not** a conflict (TMDb files them under `tv`); it
  is a mild positive.
- **Large year gap** (≥ 4 years, both years known) — caps the score at `0.70`, so a wrong-year match
  cannot be reported as high confidence. This is what separates *Fullmetal Alchemist* (2003) from
  *Brotherhood* (2009).

Episode counts are a **soft signal only**: AniList counts per season while TMDb often counts every
aired episode, so a large difference is common and legitimate. The series-vs-film distinction is
already handled by the format signal, so episodes do not add a second veto.

```
finalScore = clamp( min(raw, cap_format, cap_year), 0, 1 )
penaltyScore = raw − finalScore        (reported for transparency)
```

## Decisions

`MatchThresholds.decide(finalScore, exact, hardConflict)` maps the score to a decision. `EXACT_MATCH`
additionally requires identical normalized titles **and** no hard conflict. Ambiguity (best within
`0.05` of the runner-up) is decided by the matcher, which alone knows the runner-up's score.

| Decision | Default condition |
| --- | --- |
| `EXACT_MATCH` | exact normalized title, score ≥ 0.95, no hard conflict |
| `HIGH_CONFIDENCE` | score ≥ 0.85 |
| `MEDIUM_CONFIDENCE` | score ≥ 0.70 |
| `LOW_CONFIDENCE` | score ≥ 0.55 |
| `NO_MATCH` | score < 0.55 |
| `AMBIGUOUS` | best − runner-up < 0.05, both ≥ 0.55 |

All weights, thresholds and caps live in `ScoringWeights` and `MatchThresholds` and can be replaced
per matcher or per request.
