# Normalization

Before any comparison, every title is run through `TitleNormalizer`, which produces a
`NormalizedTitle` holding the **original** text (never lost), the cleaned `normalized` string, its
ordered `tokens` and `tokenSet`, and the extracted `TitleSequence` metadata.

## Pipeline

The steps run in this order; each can be toggled through `TitleNormalizationOptions`.

1. **Lower-case** the title.
2. **Remove diacritics** via Unicode NFD decomposition, then strip combining marks
   (`Pokémon` → `pokemon`, `Tōkyō` → `tokyo`). Non-Latin scripts (kana, kanji) are left intact.
3. **Remove bracketed noise** such as `(TV)` or `[2021]`.
4. **Expand `&`** to the word `and`.
5. **Delete apostrophes** without splitting words (`journey's` → `journeys`).
6. **Strip remaining symbols** to spaces, keeping letters and digits of any script
   (`Fate/stay night` → `fate stay night`, `Steins;Gate` → `steins gate`).
7. **Convert roman numerals** (`II` → `2`), leaving the ambiguous single letters `i, l, c, d, m`
   untouched so ordinary words are not corrupted.
8. **Extract sequence and format markers** into metadata (see below).
9. **Remove stopwords** (`the`, `vol`, `chapter`, `episode`, `arc`, …) from the similarity string —
   but only if something is left afterwards.

## Worked examples

| Input | Normalized string | Extracted metadata |
| --- | --- | --- |
| `Kimetsu no Yaiba: Yuukaku-hen` | `kimetsu no yaiba yuukaku hen` | – |
| `Shingeki no Kyojin Season 3 Part 2` | `shingeki no kyojin` | season 3, part 2 |
| `Boku no Hero Academia 2nd Season` | `boku no hero academia` | season 2 |
| `Kaguya-sama wa Kokurasetai: Ultra Romantic` | `kaguya sama wa kokurasetai ultra romantic` | – |
| `Fate/stay night: Unlimited Blade Works` | `fate stay night unlimited blade works` | – |
| `Kara no Kyoukai: The Movie` | `kara no kyoukai` | format hint: MOVIE |

## Why markers are kept as metadata

Season, part, cour and format words are a primary cause of mismatches, so they are **parsed, not
discarded**. Stripping them from the similarity string lets `Jujutsu Kaisen 2nd Season` match the
TMDb show entry `Jujutsu Kaisen`, while the structured `TitleSequence` (season 2) is preserved and
handed to the scorer as a dedicated season signal. Format hints (a trailing `Movie`, `OVA`, `Special`)
are only recognised at the end of a title, so a leading word like the `Special` in `Special A` is
left alone.
