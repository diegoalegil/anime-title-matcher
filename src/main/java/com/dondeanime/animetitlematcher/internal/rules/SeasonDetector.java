package com.dondeanime.animetitlematcher.internal.rules;

import com.dondeanime.animetitlematcher.internal.support.TokenUtils;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts season, part, cour and format markers from a title, returning the structured
 * {@link TitleSequence} together with a clean base title.
 *
 * <p>Input is expected to be already lowercased and symbol-stripped (separators turned into
 * spaces) by the caller, e.g. {@code "shingeki no kyojin season 3 part 2"}. The detector
 * understands numeric forms ({@code "season 2"}, {@code "2nd season"}, {@code "part 2"},
 * {@code "cour 2"}), spelled-out forms ({@code "second season"}, {@code "part two"}) and the
 * {@code "final season"} / {@code "final part"} marker.
 *
 * <p>Format hints ({@code movie}, {@code ova}, {@code ona}, {@code special}) are only recognised
 * at the end of the title, so an ordinary leading word such as the {@code "Special"} in
 * {@code "Special A"} is preserved.
 *
 * <p>This class is part of the internal implementation and is not a stable public API.
 */
public final class SeasonDetector {

    private static final Map<String, Integer> WORD_NUMBERS = Map.ofEntries(
            Map.entry("one", 1), Map.entry("first", 1),
            Map.entry("two", 2), Map.entry("second", 2),
            Map.entry("three", 3), Map.entry("third", 3),
            Map.entry("four", 4), Map.entry("fourth", 4),
            Map.entry("five", 5), Map.entry("fifth", 5),
            Map.entry("six", 6), Map.entry("sixth", 6),
            Map.entry("seven", 7), Map.entry("seventh", 7),
            Map.entry("eight", 8), Map.entry("eighth", 8),
            Map.entry("nine", 9), Map.entry("ninth", 9),
            Map.entry("ten", 10), Map.entry("tenth", 10),
            Map.entry("eleven", 11), Map.entry("eleventh", 11),
            Map.entry("twelve", 12), Map.entry("twelfth", 12));

    private static final String WORD_ALTERNATION = String.join("|", WORD_NUMBERS.keySet());

    private static final Pattern FINAL_MARKER = Pattern.compile("\\bfinal\\s+(?:season|part)\\b");
    private static final Pattern SEASON_NUM = Pattern.compile("\\b(?:seasons?)\\s+(\\d{1,2})\\b");
    private static final Pattern NUM_SEASON = Pattern.compile("\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+seasons?\\b");
    private static final Pattern WORD_SEASON = Pattern.compile("\\b(" + WORD_ALTERNATION + ")\\s+seasons?\\b");
    private static final Pattern COUR_NUM = Pattern.compile("\\bcour\\s+(\\d{1,2})\\b");
    private static final Pattern PART_NUM = Pattern.compile("\\bpart\\s+(\\d{1,2})\\b");
    private static final Pattern WORD_PART = Pattern.compile("\\bpart\\s+(" + WORD_ALTERNATION + ")\\b");

    private record HintPattern(Pattern pattern, FormatHint hint) {
    }

    private static final List<HintPattern> HINT_PATTERNS = List.of(
            new HintPattern(Pattern.compile("\\b(?:the\\s+)?movie\\s*$"), FormatHint.MOVIE),
            new HintPattern(Pattern.compile("\\bova\\s*$"), FormatHint.OVA),
            new HintPattern(Pattern.compile("\\bona\\s*$"), FormatHint.ONA),
            new HintPattern(Pattern.compile("\\bspecials?\\s*$"), FormatHint.SPECIAL));

    private SeasonDetector() {
    }

    public static TitleSequence detect(String input) {
        if (input == null || input.isBlank()) {
            return TitleSequence.none("");
        }

        String working = input.toLowerCase(Locale.ROOT);
        boolean finalMarker = false;

        Matcher matcher = FINAL_MARKER.matcher(working);
        if (matcher.find()) {
            finalMarker = true;
            working = removeMatch(working, matcher);
        }

        Integer season = null;
        matcher = SEASON_NUM.matcher(working);
        if (matcher.find()) {
            season = Integer.parseInt(matcher.group(1));
            working = removeMatch(working, matcher);
        }
        if (season == null) {
            matcher = NUM_SEASON.matcher(working);
            if (matcher.find()) {
                season = Integer.parseInt(matcher.group(1));
                working = removeMatch(working, matcher);
            }
        }
        if (season == null) {
            matcher = WORD_SEASON.matcher(working);
            if (matcher.find()) {
                season = WORD_NUMBERS.get(matcher.group(1));
                working = removeMatch(working, matcher);
            }
        }

        Integer cour = null;
        matcher = COUR_NUM.matcher(working);
        if (matcher.find()) {
            cour = Integer.parseInt(matcher.group(1));
            working = removeMatch(working, matcher);
        }

        Integer part = null;
        matcher = PART_NUM.matcher(working);
        if (matcher.find()) {
            part = Integer.parseInt(matcher.group(1));
            working = removeMatch(working, matcher);
        }
        if (part == null) {
            matcher = WORD_PART.matcher(working);
            if (matcher.find()) {
                part = WORD_NUMBERS.get(matcher.group(1));
                working = removeMatch(working, matcher);
            }
        }

        FormatHint hint = FormatHint.NONE;
        for (HintPattern hintPattern : HINT_PATTERNS) {
            matcher = hintPattern.pattern().matcher(working);
            if (matcher.find()) {
                hint = hintPattern.hint();
                working = removeMatch(working, matcher);
                break;
            }
        }

        String baseTitle = TokenUtils.collapseWhitespace(working);
        return new TitleSequence(season, part, cour, finalMarker, hint, baseTitle);
    }

    private static String removeMatch(String source, Matcher matcher) {
        return source.substring(0, matcher.start()) + " " + source.substring(matcher.end());
    }
}
