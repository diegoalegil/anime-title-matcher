package com.dondeanime.animetitlematcher.api;

import com.dondeanime.animetitlematcher.domain.AniListAnime;
import com.dondeanime.animetitlematcher.domain.TmdbTitle;
import com.dondeanime.animetitlematcher.internal.normalize.NormalizedVariant;
import com.dondeanime.animetitlematcher.internal.normalize.TitleNormalizer;
import com.dondeanime.animetitlematcher.internal.normalize.VariantNormalizer;
import com.dondeanime.animetitlematcher.internal.score.MatchScorer;
import com.dondeanime.animetitlematcher.internal.score.ScoreResult;
import com.dondeanime.animetitlematcher.internal.similarity.CompositeTitleSimilarity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Entry point of the library: finds the most probable TMDb equivalent of an AniList anime from a
 * supplied list of candidates, and explains why.
 *
 * <p>Create one with {@link #createDefault()} (or {@link #create(ScoringWeights, MatchThresholds)}
 * to tune scoring) and reuse it; instances are immutable and thread-safe. The AniList titles are
 * normalised once per call and reused across every candidate.
 *
 * <pre>{@code
 * AnimeTitleMatcher matcher = AnimeTitleMatcher.createDefault();
 * MatchResult result = matcher.findBestMatch(anime, candidates);
 * if (result.isMatched()) {
 *     System.out.println(result.bestCandidate().tmdbTitle().title());
 * }
 * }</pre>
 */
public final class AnimeTitleMatcher {

    private final VariantNormalizer variantNormalizer;
    private final CompositeTitleSimilarity similarity;
    private final ScoringWeights weights;
    private final MatchThresholds thresholds;

    private AnimeTitleMatcher(ScoringWeights weights, MatchThresholds thresholds) {
        this.variantNormalizer = new VariantNormalizer(new TitleNormalizer());
        this.similarity = new CompositeTitleSimilarity();
        this.weights = Objects.requireNonNull(weights, "weights");
        this.thresholds = Objects.requireNonNull(thresholds, "thresholds");
    }

    /** A matcher with default scoring weights and thresholds. */
    public static AnimeTitleMatcher createDefault() {
        return new AnimeTitleMatcher(ScoringWeights.defaults(), MatchThresholds.defaults());
    }

    /** A matcher with custom weights and thresholds. */
    public static AnimeTitleMatcher create(ScoringWeights weights, MatchThresholds thresholds) {
        return new AnimeTitleMatcher(weights, thresholds);
    }

    /** Finds the best TMDb match for {@code anime} among {@code candidates}. */
    public MatchResult findBestMatch(AniListAnime anime, List<TmdbTitle> candidates) {
        return match(new MatchRequest(anime, candidates, weights, thresholds));
    }

    /** Finds the best TMDb match described by {@code request}. */
    public MatchResult match(MatchRequest request) {
        Objects.requireNonNull(request, "request");
        AniListAnime anime = Objects.requireNonNull(request.anime(), "request.anime");
        ScoringWeights effectiveWeights = request.weights() != null ? request.weights() : weights;
        MatchThresholds effectiveThresholds = request.thresholds() != null ? request.thresholds() : thresholds;
        MatchScorer scorer = new MatchScorer(similarity, effectiveWeights, effectiveThresholds);

        List<TmdbTitle> candidates = request.candidates();
        if (candidates.isEmpty()) {
            return emptyResult();
        }

        List<NormalizedVariant> anilistVariants = variantNormalizer.normalize(anime.titleVariants());

        record Scored(TmdbTitle tmdb, ScoreResult result, int index) {
        }
        List<Scored> scored = new ArrayList<>(candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            TmdbTitle candidate = candidates.get(i);
            List<NormalizedVariant> tmdbVariants = variantNormalizer.normalize(candidate.titleVariants());
            scored.add(new Scored(candidate, scorer.score(anime, anilistVariants, candidate, tmdbVariants), i));
        }
        scored.sort(Comparator
                .comparingDouble((Scored s) -> s.result().finalScore()).reversed()
                .thenComparingLong(s -> s.tmdb().id())
                .thenComparingInt(Scored::index));

        List<MatchCandidate> allCandidates = scored.stream()
                .map(s -> toCandidate(s.tmdb(), s.result()))
                .toList();

        Scored best = scored.get(0);
        double bestScore = best.result().finalScore();
        MatchDecision decision = effectiveThresholds.decide(
                bestScore, best.result().exactMatch(), best.result().hardConflict());

        List<String> warnings = new ArrayList<>();
        if (decision.isMatch() && scored.size() > 1) {
            Scored runnerUp = scored.get(1);
            double gap = bestScore - runnerUp.result().finalScore();
            if (gap < effectiveThresholds.ambiguityDelta()
                    && runnerUp.result().finalScore() >= effectiveThresholds.lowConfidence()) {
                decision = MatchDecision.AMBIGUOUS;
                warnings.add(String.format(Locale.ROOT,
                        "Top two candidates are within %.3f (%.3f vs %.3f); treat the result as ambiguous.",
                        effectiveThresholds.ambiguityDelta(), bestScore, runnerUp.result().finalScore()));
            }
        }

        MatchCandidate bestCandidate = allCandidates.get(0);
        MatchExplanation explanation = explain(best.tmdb(), best.result(), decision, bestScore, effectiveThresholds);
        return new MatchResult(decision, bestCandidate, allCandidates, warnings, explanation);
    }

    private MatchCandidate toCandidate(TmdbTitle tmdb, ScoreResult result) {
        return new MatchCandidate(
                tmdb,
                result.finalScore(),
                result.breakdown(),
                titleOf(result.matchedAnilistVariant()),
                titleOf(result.matchedTmdbVariant()),
                result.hardConflict());
    }

    private MatchExplanation explain(TmdbTitle tmdb, ScoreResult result, MatchDecision decision,
                                     double score, MatchThresholds thresholds) {
        ScoreBreakdown breakdown = result.breakdown();
        List<String> signals = new ArrayList<>();
        signals.add("title");
        if (breakdown.seasonScore() != null) {
            signals.add("season");
        }
        if (breakdown.yearScore() != null) {
            signals.add("year");
        }
        if (breakdown.formatScore() != null) {
            signals.add("format");
        }
        if (breakdown.episodeScore() != null) {
            signals.add("episodes");
        }

        String matchedAnilist = titleOf(result.matchedAnilistVariant());
        String matchedTmdb = titleOf(result.matchedTmdbVariant());
        String rejectionReason = switch (decision) {
            case NO_MATCH -> String.format(Locale.ROOT,
                    "Best score %.2f is below the low-confidence threshold of %.2f.",
                    score, thresholds.lowConfidence());
            case AMBIGUOUS -> "A runner-up candidate scored within the ambiguity delta.";
            default -> null;
        };

        String summary = switch (decision) {
            case NO_MATCH -> String.format(Locale.ROOT,
                    "No reliable match: closest was TMDb \"%s\" (id %d) at %.2f.",
                    tmdb.title(), tmdb.id(), score);
            case AMBIGUOUS -> String.format(Locale.ROOT,
                    "Ambiguous best match TMDb \"%s\" (id %d) at %.2f; another candidate scored almost as high.",
                    tmdb.title(), tmdb.id(), score);
            default -> String.format(Locale.ROOT,
                    "%s: matched \"%s\" to TMDb \"%s\" (id %d) at %.2f.",
                    decision, matchedAnilist == null ? "" : matchedAnilist, tmdb.title(), tmdb.id(), score);
        };

        return new MatchExplanation(summary, matchedAnilist, matchedTmdb, signals, rejectionReason);
    }

    private MatchResult emptyResult() {
        MatchExplanation explanation = new MatchExplanation(
                "No candidates to evaluate.", null, null, List.of(), "No TMDb candidates were provided.");
        return new MatchResult(MatchDecision.NO_MATCH, null, List.of(),
                List.of("No TMDb candidates were provided."), explanation);
    }

    private static String titleOf(com.dondeanime.animetitlematcher.domain.TitleVariant variant) {
        return variant == null ? null : variant.value();
    }
}
