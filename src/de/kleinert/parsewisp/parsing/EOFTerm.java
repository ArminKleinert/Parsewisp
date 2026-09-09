package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import de.kleinert.parsewisp.trampoline.TrampolineListenerNode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class should almost never be used, but can have its uses occasionally.
 * <p>
 * Format: {@code "EOF"}
 */
public final class EOFTerm extends Terminal {
    private EOFTerm(final boolean hide, final @NotNull ReductionType red) {
        super(hide, red);
    }

    /**
     * Default text for EOF.
     * @return The text that identifies an EOF rule in a grammar.
     */
    public static @NotNull String text () {
        return "EOF";
    }

    /**
     * Default EOF rule. Can be buffered.
     *
     * @return Default EOF rule. Can be buffered.
     */
    public static @NotNull EOFTerm getDefault() {
        return new EOFTerm(defaultHidden, defaultReductionType);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        fullParse(index, runner);
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        if (index == runner.tramp().getText().length())
            runner.pushSuccessMessageWithoutValue(new TrampolineListenerNode.TrampolineListenerKey(index, this), index);
        else
            runner.fail(new TrampolineListenerNode.TrampolineListenerKey(index, this), index,
                    ParseFailureReason.ofEpsilon(EpsilonTerm.getDefault(), true));
    }

    @Override
    public @NotNull EOFTerm withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new EOFTerm(hide, red);
    }

    @Override
    public @NotNull EOFTerm withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new EOFTerm(hide, red);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EOFTerm that)) return false;
        if (this == that) return true;
        return hide == that.hide && Objects.equals(red, that.red);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, red);
    }
}
