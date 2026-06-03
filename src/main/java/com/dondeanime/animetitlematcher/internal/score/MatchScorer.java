package com.dondeanime.animetitlematcher.internal.score;

import com.dondeanime.animetitlematcher.api.MatchThresholds;
import com.dondeanime.animetitlematcher.api.ScoreBreakdown;
import com.dondeanime.animetitlematcher.api.ScoringWeights;
import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.TitleVariant;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import com.dondeanime.animetitlematcher.internal.normalize.NormalizedVariant;
import com.dondeanime.animetitlematcher.internal.rules.EpisodeCountComparator;
import com.dondeanime.animetitlematcher.internal.rules.FormatHint;
import com.dondeanime.animetitlematcher.internal.rules.FormatMapper;
import com.dondeanime.animetitlematcher.internal.rules.SequenceComparator;
import com.dondeanime.animetitlematcher.internal.rules.TitleSequence;
import com.dondeanime.animetitlematcher.internal.rules.YearComparator;
import com.dondeanime.animetitlematcher.internal.similarity.CompositeTitleSimilarity;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Scores one {@link TmdbTitle} candidate against one {@link AniListAnime} across every signal and
 * combines them into an explainable {@link ScoreResult}.
 *
 * <p>The title signal is the best composite similarity over the normalised variant pairs, with
 * alternative-title matches discounted slightly so a canonical match wins ties. The remaining
 * signals (season, year, format, episodes) are added as a weighted average that is renormalised
 * over whichever signals are present. Hard conflicts (TV vs movie, large year gap) do not add a
 * second penalty: the conflicting signal is dropped from the average and the final score is capped
 * instead, so the conflict is counted exactly once.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class MatchScorer {

    private static final double ALTERNATIVE_DISCOUNT = 0.97;

    private final CompositeTitleSimilarity similarity;
    private final ScoringWeights weights;
    private final MatchThresholds thresholds;

    public MatchScorer(CompositeTitleSimilarity similarity, ScoringWeights weights, MatchThresholds thresholds) {
        this.similarity = similarity;
        this.weights = weights;
        this.thresholds = thresholds;
    }

    public ScoreResult score(AniListAnime anime, List<NormalizedVariant> anilistVariants,
                             TmdbTitle tmdb, List<NormalizedVariant> tmdbVariants) {
        double primaryBest = 0.0;
        double alternativeBest = 0.0;
        double effectiveBest = 0.0;
        boolean exact = false;
        TitleVariant matchedAnilist = null;
        TitleVariant matchedTmdb = null;

        for (NormalizedVariant anilist : anilistVariants) {
            for (NormalizedVariant candidate : tmdbVariants) {
                double sim = similarity.similarity(anilist.normalized(), candidate.normalized());
                boolean bothPrimary = anilist.variant().isPrimary() && candidate.variant().isPrimary();
                double adjusted = bothPrimary ? sim : sim * ALTERNATIVE_DISCOUNT;
                if (adjusted > effectiveBest) {
                    effectiveBest = adjusted;
                    matchedAnilist = anilist.variant();
                    matchedTmdb = candidate.variant();
                }
                if (bothPrimary) {
                    primaryBest = Math.max(primaryBest, sim);
                } else {
                    alternativeBest = Math.max(alternativeBest, sim);
                }
                if (anilist.normalized().normalized().equals(candidate.normalized().normalized())) {
                    exact = true;
                }
            }
        }
        double effectiveTitle = effectiveBest;

        FormatHint anilistHint = firstFormatHint(anilistVariants);
        OptionalDouble seasonScore = SequenceComparator.score(
                bestSequence(anilistVariants), bestSequence(tmdbVariants));
        OptionalDouble yearScore = YearComparator.score(anime.year(), tmdb.year());
        boolean yearConflict = YearComparator.isHardConflict(anime.year(), tmdb.year());
        OptionalDouble formatScore = FormatMapper.score(anime.format(), anilistHint, tmdb.mediaType());
        boolean formatConflict = FormatMapper.isHardConflict(anime.format(), anilistHint, tmdb.mediaType());
        OptionalDouble episodeScore = EpisodeCountComparator.score(anime.episodes(), tmdb.episodeCount());

        double weightSum = weights.title();
        double weightedSum = weights.title() * effectiveTitle;
        if (seasonScore.isPresent()) {
            weightSum += weights.season();
            weightedSum += weights.season() * seasonScore.getAsDouble();
        }
        if (yearScore.isPresent() && !yearConflict) {
            weightSum += weights.year();
            weightedSum += weights.year() * yearScore.getAsDouble();
        }
        if (formatScore.isPresent() && !formatConflict) {
            weightSum += weights.format();
            weightedSum += weights.format() * formatScore.getAsDouble();
        }
        if (episodeScore.isPresent()) {
            weightSum += weights.episodes();
            weightedSum += weights.episodes() * episodeScore.getAsDouble();
        }
        double raw = weightSum > 0 ? weightedSum / weightSum : 0.0;

        double cap = 1.0;
        if (formatConflict) {
            cap = Math.min(cap, thresholds.formatConflictCap());
        }
        if (yearConflict) {
            cap = Math.min(cap, thresholds.yearConflictCap());
        }
        double finalScore = clamp(Math.min(raw, cap));
        double penalty = Math.max(0.0, raw - finalScore);
        boolean hardConflict = formatConflict || yearConflict;

        ScoreBreakdown breakdown = new ScoreBreakdown(
                primaryBest,
                alternativeBest,
                boxed(seasonScore),
                boxed(yearScore),
                boxed(formatScore),
                boxed(episodeScore),
                penalty,
                finalScore);

        boolean exactMatch = exact && effectiveTitle >= thresholds.exactMatch();
        return new ScoreResult(breakdown, exactMatch, hardConflict, matchedAnilist, matchedTmdb);
    }

    private static TitleSequence bestSequence(List<NormalizedVariant> variants) {
        for (NormalizedVariant variant : variants) {
            if (variant.normalized().sequence().hasSequenceInfo()) {
                return variant.normalized().sequence();
            }
        }
        return TitleSequence.none("");
    }

    private static FormatHint firstFormatHint(List<NormalizedVariant> variants) {
        for (NormalizedVariant variant : variants) {
            FormatHint hint = variant.normalized().sequence().formatHint();
            if (hint != FormatHint.NONE) {
                return hint;
            }
        }
        return FormatHint.NONE;
    }

    private static Double boxed(OptionalDouble value) {
        return value.isPresent() ? value.getAsDouble() : null;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
