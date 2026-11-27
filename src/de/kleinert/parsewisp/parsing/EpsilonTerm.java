package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.Reduction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents the empty parse epsilon. It can typically be written in the following ways: "Epsilon", "epsilon", "EPSILON", "eps", "ε" or as the empty string "".
 * <br/>
 * When parsing, the process depends on whether a full parse is being done (matching to the end of the input):
 * If yes, success if the end of input has been reached, fail otherwise. If no, always success.
 *
 * @since 0.9.7
 */
public final class EpsilonTerm extends Terminal {
    //private static final @NotNull EpsilonTerm epsilon = new EpsilonTerm(defaultHidden, defaultReductionType);

    private EpsilonTerm(final boolean hide, final @NotNull Reduction red) {
        super(hide, red);
    }

    /**
     * A default instance of Epsilon. May be buffered, but does not have to.
     *
     * @return A default instance of Epsilon.
     * @since 0.9.7
     */
    public static @NotNull EpsilonTerm getDefault() {
        //return epsilon;
        return new EpsilonTerm(DEFAULT_HIDDEN, DEFAULT_REDUCTION);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        runner.pushSuccessMessageWithoutValue(makeKey(index, this), index);
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        if (index == runner.tramp().getText().length())
            runner.pushSuccessMessageWithoutValue(makeKey(index, this), index);
        else
            runner.fail(makeKey(index, this), index, this, true);
    }

    @Override
    public @NotNull EpsilonTerm withHideTag(final boolean hide) {
        return isHiddenInResult() == hide ? this : new EpsilonTerm(hide, reduction);
    }

    @Override
    public @NotNull EpsilonTerm withReduction(final @NotNull Reduction red) {
        return getReduction() == red ? this : new EpsilonTerm(hide, red);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EpsilonTerm that)) return false;
        if (this == that) return true;
        return hide == that.hide && Objects.equals(reduction, that.reduction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, reduction);
    }
}
