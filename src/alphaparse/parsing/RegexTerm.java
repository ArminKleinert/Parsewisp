package alphaparse.parsing;

import static alphaparse.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import alphaparse.parser_options.ParsingOptions;
import alphaparse.reduction.ReductionType;
import alphaparse.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents terminal expressions from regular expressions.
 * This class has the property of being both more powerful and uniquely dangerous to use.
 * When parsing, the regex is eagerly, not lazily, resolved.
 * <br/>
 * This means that the regex {@code "a+"} on the input "aaaab" will always match all four "a"s,
 * instead of returning different results "a", "aa", "aaa" and "aaaa".
 * <br/>
 * Syntax: A string literal prefixed with a hash-symbol: {@code #"..."} and {@code #'...'} are equivalent.
 */
public final class RegexTerm extends Terminal {
    private final @NotNull Pattern regexp;

    private RegexTerm(final boolean hide,
                      final @NotNull ReductionType red,
                      final @NotNull Pattern regexp) {
        super(hide, red);
        this.regexp = regexp;
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param regexp The pattern.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull Pattern regexp) {
        var s = regexp.pattern();
        if (s.chars().allMatch(Character::isLetterOrDigit))
            return StringTerm.create(s, false);
        return new RegexTerm(defaultHidden, defaultReductionType, regexp);
    }

    private static @Nullable String reMatchAtFront(final @NotNull Pattern regexp,
                                                   final @NotNull CharSequence text) {
        final @NotNull Matcher matcher = regexp.matcher(text);
        if (matcher.lookingAt()) return matcher.group();
        return null;
    }

    /**
     * Parse the string using the regex. Normally, the longest match is eagerly taken.
     * So by default, the following is true:
     * <pre>
     * {@code
     *         var p = Alpha.parser("S : #'A+' 'A'");
     *         Assertions.assertTrue(p.parse("AAA").isFailure()); // Parsing failed because #'A+' eagerly matched the entire input.
     * }
     * </pre>
     * If the {@link ParsingOptions#iterativeDeepening()} option is true, the parse would succeed.
     * <pre>
     * {@code
     *         var p = Alpha.parser("S : #'A+' 'A'");
     *         var opts = ParsingOptions.getDefault().withIterativeDeepening(true);
     *         Assertions.assertEquals(ParseTree.create("S", "AA", "A"), p.parse("AAA", opts));
     * }
     * </pre>
     *
     * @param index  The start index.
     * @param runner Helper structure.
     */
    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull Pattern regexp = getRegexp();
        final @NotNull String text = runner.tramp().getText();
        final @NotNull String subString = text.substring(index);
        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, this);
        final @Nullable String firstMatch = reMatchAtFront(regexp, subString);

        if (firstMatch == null) {
            runner.fail(nodeKey, index, ParseFailureReason.ofRegexTerminal(this, false));
            return;
        }

        if (!runner.iterativeDeepening()) {
            runner.pushSuccessMessage(nodeKey, firstMatch, index + firstMatch.length());
            return;
        }

        final int endIndexInSubString = firstMatch.length();
        runner.pushSuccessMessage(nodeKey, firstMatch, index + firstMatch.length());
        for (int end = endIndexInSubString; end >= 0; end--) {
            @Nullable String match = reMatchAtFront(regexp, subString.substring(0, end));
            if (match != null) {
                runner.pushSuccessMessage(nodeKey, match, index + match.length());
            }
        }
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull Pattern regexp = this.getRegexp();
        final @NotNull String text = runner.tramp().getText();
        final @NotNull String substring = text.substring(index);
        final @Nullable String match = reMatchAtFront(regexp, substring);
        final int desiredLength = text.length() - index;
        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, this);
        if (match != null && match.length() == desiredLength) {
            runner.pushSuccessMessage(nodeKey, match, text.length());
        } else {
            runner.fail(nodeKey, index, ParseFailureReason.ofRegexTerminal(this, true));
        }
    }

    /**
     * The regex/pattern.
     *
     * @return The regex.
     */
    public @NotNull Pattern getRegexp() {
        return regexp;
    }

    @Override
    public @NotNull RegexTerm withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new RegexTerm(hide, red, regexp);
    }

    @Override
    public @NotNull RegexTerm withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new RegexTerm(hide, red, regexp);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegexTerm that)) return false;
        if (this == that) return true;
        return hide == that.hide
                && Objects.equals(red, that.red)
                && Objects.equals(regexp.pattern(), that.getRegexp().pattern());
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, red, regexp.pattern());
    }
}
