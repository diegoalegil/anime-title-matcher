package com.dondeanime.animetitlematcher.internal.rules;

import com.dondeanime.animetitlematcher.domain.AnimeFormat;
import com.dondeanime.animetitlematcher.domain.TmdbMediaType;
import java.util.OptionalDouble;

/**
 * Compares an AniList format against a TMDb media type.
 *
 * <p>TMDb only distinguishes {@code tv} and {@code movie}, and routinely files OVAs, ONAs and
 * specials under {@code tv}. Therefore the only <em>hard</em> cross-type conflict is a series
 * ({@code TV}) versus a {@code movie}: that pairing scores {@code 0.0} and is reported as a hard
 * conflict (which caps the final score). OVA/ONA/SPECIAL against {@code tv} is a mild positive, not
 * a conflict. When the AniList format is unknown it is inferred from a title-derived
 * {@link FormatHint}; if it is still unknown, or the TMDb type is unknown, the signal is absent.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class FormatMapper {

    private FormatMapper() {
    }

    public static OptionalDouble score(AnimeFormat format, FormatHint hint, TmdbMediaType tmdbType) {
        AnimeFormat effective = resolve(format, hint);
        if (effective == AnimeFormat.UNKNOWN || effective == AnimeFormat.MUSIC || tmdbType == TmdbMediaType.UNKNOWN) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(rawScore(effective, tmdbType));
    }

    public static boolean isHardConflict(AnimeFormat format, FormatHint hint, TmdbMediaType tmdbType) {
        AnimeFormat effective = resolve(format, hint);
        if (tmdbType == TmdbMediaType.UNKNOWN) {
            return false;
        }
        boolean seriesVsFilm = effective == AnimeFormat.TV && tmdbType == TmdbMediaType.MOVIE;
        boolean filmVsSeries = effective == AnimeFormat.MOVIE && tmdbType == TmdbMediaType.TV;
        return seriesVsFilm || filmVsSeries;
    }

    private static double rawScore(AnimeFormat effective, TmdbMediaType tmdbType) {
        return switch (effective) {
            case TV -> tmdbType == TmdbMediaType.TV ? 1.0 : 0.0;
            case MOVIE -> tmdbType == TmdbMediaType.MOVIE ? 1.0 : 0.0;
            case OVA, ONA, SPECIAL -> tmdbType == TmdbMediaType.TV ? 0.70 : 0.45;
            default -> 0.50;
        };
    }

    private static AnimeFormat resolve(AnimeFormat format, FormatHint hint) {
        if (format != null && format != AnimeFormat.UNKNOWN) {
            return format;
        }
        if (hint == null) {
            return AnimeFormat.UNKNOWN;
        }
        return switch (hint) {
            case MOVIE -> AnimeFormat.MOVIE;
            case OVA -> AnimeFormat.OVA;
            case ONA -> AnimeFormat.ONA;
            case SPECIAL -> AnimeFormat.SPECIAL;
            case NONE -> AnimeFormat.UNKNOWN;
        };
    }
}
