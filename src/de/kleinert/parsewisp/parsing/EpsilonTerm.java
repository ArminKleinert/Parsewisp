package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

/**
 * This class represents the empty parse epsilon. It can typically be written in the following ways: "ε"
 * <br/>
 * Which specific formats are valid can be dictated by using {@link ParserCreationOptions#epsilonNames()} appropriately. See also: {@link ParserCreationOptions#defaultEpsilonNames()}
 * <br/>
 * When parsing, the process depends on whether a full parse is being done (matching to the end of the input):
 * If yes, success if the end of input has been reached, fail otherwise. If no, always success.
 */
public final class EpsilonTerm extends Terminal {
    private static final @NotNull EpsilonTerm epsilon = new EpsilonTerm(defaultHidden, defaultReductionType);

    private EpsilonTerm(final boolean hide, final @NotNull ReductionType red) {
        super(hide, red);
    }

    /**
     * The default is always buffered. {@link #getReduction()} and {@link #isHidden()} have their default values.
     *
     * @return The canonical instance of Epsilon.
     */
    public static @NotNull EpsilonTerm getDefault() {
        return epsilon;
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        runner.pushSuccessMessageWithoutValue(new TrampolineListenerKey(index, this), index);
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        if (index == runner.tramp().getText().length())
            runner.pushSuccessMessageWithoutValue(new TrampolineListenerKey(index, this), index);
        else
            runner.fail(new TrampolineListenerKey(index, this), index,
                    ParseFailureReason.ofEpsilon(this, true));
    }

    @Override
    public @NotNull EpsilonTerm withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new EpsilonTerm(hide, red);
    }

    @Override
    public @NotNull EpsilonTerm withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new EpsilonTerm(hide, red);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EpsilonTerm that)) return false;
        if (this == that) return true;
        return hide == that.hide && Objects.equals(red, that.red);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, red);
    }
}
