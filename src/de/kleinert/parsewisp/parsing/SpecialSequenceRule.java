package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

/**
 * Represents a special sequence. The typical EBNF-notation is {@code ?...?} where {@code ...} stands for some free-form text.
 * For example, {@code S = ?any whitespace except newline?} means what it says. This allows some interaction with the program around the parser.
 * <p>
 * You should not use this class unless it is very important!
 */
public final class SpecialSequenceRule extends SimpleRule {
    private final @NotNull String description;
    private final @NotNull Function<@NotNull String, Optional<String>> function;

    private SpecialSequenceRule(final boolean hide,
                                final @NotNull ReductionType red,
                                final @NotNull String description,
                                final @NotNull Function<@NotNull String, Optional<String>> function) {
        super(hide, red);
        this.function = function;
        this.description = description;
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param description The description of the special sequence.
     * @param function    The function which does what the description says.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull String description,
                                       final @NotNull Function<@NotNull String, Optional<String>> function) {
        return new SpecialSequenceRule(defaultHidden, defaultReductionType, description, function);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull String text = runner.tramp().getText().substring(index);
        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, this);
        var res = function.apply(text);

        if (res.isEmpty()) {
            runner.fail(nodeKey, index, ParseFailureReason.ofSpecialSequence(this, false));
            return;
        }

        runner.pushSuccessMessage(nodeKey, res.get(), index + res.get().length());
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull String text = runner.tramp().getText().substring(index);
        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, this);
        var res = function.apply(text);

        if (res.isEmpty()) {
            runner.fail(nodeKey, index, ParseFailureReason.ofSpecialSequence(this, true));
            return;
        }

        runner.pushSuccessMessage(nodeKey, res.get(), index + res.get().length());
    }

    @Override
    public @NotNull SpecialSequenceRule withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new SpecialSequenceRule(hide, red, description, function);
    }

    @Override
    public @NotNull SpecialSequenceRule withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new SpecialSequenceRule(hide, red, description, function);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpecialSequenceRule that)) return false;
        if (this == that) return true;
        return hide == that.hide
                && Objects.equals(red, that.red)
                && Objects.equals(function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, red, function);
    }

    @Override
    public String toString() {
        return description;
    }
}
