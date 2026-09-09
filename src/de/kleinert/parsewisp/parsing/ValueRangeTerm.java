package de.kleinert.parsewisp.parsing;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an ABNF value range.
 */
public final class ValueRangeTerm extends Terminal {
    private final int lo;
    private final int hi;

    private ValueRangeTerm(final boolean hide,
                           final @NotNull ReductionType red,
                           final int lo, final int hi) {
        super(hide, red);
        if (lo > hi) throw new IllegalArgumentException();
        this.lo = lo;
        this.hi = hi;
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param lo The lowest codepoint.
     * @param hi The highest codepoint.
     * @return A rule.
     * @throws IllegalArgumentException if the minimum codepoint value is greater than the maximum.
     */
    public static @NotNull Rule create(final int lo, final int hi) {
        if (lo > hi)
            throw new IllegalArgumentException();

        return new ValueRangeTerm(defaultHidden, defaultReductionType, lo, hi);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        parse(index, runner, false);
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        parse(index, runner, true);
    }

    private void parse(final int index, final @NotNull Gll runner, final boolean expectEnd) {
        final @NotNull String text = runner.tramp().getText();
        final int lo = getLo();
        final int hi = getHi();
        final int end = text.length();
        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, this);

        if (index >= text.length()) {
            runner.fail(nodeKey, index, ParseFailureReason.ofUnicodeChar(this, expectEnd));
            return;
        }

        final int codePoint = Character.codePointAt(text, index);
        final @NotNull String charString = new String(Character.toChars(codePoint));

        boolean check = !expectEnd || (index + charString.length() == end);
        if (check && lo <= codePoint && codePoint <= hi) {
            runner.pushSuccessMessage(nodeKey, charString, index + charString.length());
        } else {
            runner.fail(nodeKey, index, ParseFailureReason.ofUnicodeChar(this, expectEnd));
        }
    }

    /**
     * The lowest codepoint.
     *
     * @return The lowest codepoint as an int.
     */
    public int getLo() {
        return lo;
    }

    /**
     * The highest codepoint.
     *
     * @return The highest codepoint as an int.
     */
    public int getHi() {
        return hi;
    }

    @Override
    public @NotNull ValueRangeTerm withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new ValueRangeTerm(hide, red, lo, hi);
    }

    @Override
    public @NotNull ValueRangeTerm withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new ValueRangeTerm(hide, red, lo, hi);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ValueRangeTerm that)) return false;
        if (this == that) return true;
        return hide == that.hide
                && Objects.equals(red, that.red)
                && lo == that.lo
                && hi == that.hi;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, red, lo, hi);
    }
}
