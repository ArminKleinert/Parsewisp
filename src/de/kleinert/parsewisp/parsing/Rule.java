package de.kleinert.parsewisp.parsing;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.Print;
import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import org.jetbrains.annotations.NotNull;

/**
 * A class representing the right-hand sides of productions.
 */
public abstract sealed class Rule
        permits RuleWithManyChildren, RuleWithChild, SimpleRule {
    /**
     * Default value for {@link Rule#isHidden()}.
     */
    protected static final boolean defaultHidden = false;

    /**
     * Default value for {@link Rule#getReduction()}.
     */
    protected static final ReductionType defaultReductionType = ReductionType.standardInitialReduction();

    protected final boolean hide;
    protected final @NotNull ReductionType red;

    protected Rule(final boolean hide, final @NotNull ReductionType red) {
        this.hide = hide;
        this.red = red;
    }

    /**
     * Runs the parser from the provided index. The text is in the arguments.
     * <p>
     * Results (successes and failures) are saved using {@link Gll#pushSuccessMessage(TrampolineListenerKey, FlatResultSeq, int)} or {@link Gll#fail(TrampolineListenerKey, int, ParseFailureReason)} or some similar function.
     *
     * @param index  The start index.
     * @param runner Helper structure.
     */
    public abstract void parse(final int index, final @NotNull Gll runner);

    /**
     * Runs the parser from the provided index. The text is in the arguments. Unlike {@link Rule#parse(int, Gll)}, this method tries to parse the text from the index until the end. If the string can't be matched to the end, results in a failure.
     * <p>
     * Results (successes and failures) are saved using {@link Gll#pushSuccessMessage(TrampolineListenerKey, String, int)} or {@link Gll#fail(TrampolineListenerKey, int, ParseFailureReason)} or some similar function.
     *
     * @param index  The start index.
     * @param runner Helper structure.
     */
    public abstract void fullParse(final int index, final @NotNull Gll runner);

    /**
     * Hides or unhides content in output.
     *
     * @param hide Whether to hide the content.
     * @return An instance of the same class with the hide tag set to the parameter.
     */
    public abstract @NotNull Rule withHideTag(final boolean hide);

    /**
     * Creates an instance of this class with the reduction type set.
     *
     * @param red The reduction type.
     * @return An instance of the same class with the reduction type set to the parameter.
     */
    public abstract @NotNull Rule withReduction(final @NotNull ReductionType red);

    /**
     * Check whether the content is hidden in the output.
     *
     * @return true if the content is hidden in the output, false otherwise.
     */
    public boolean isHidden() {
        return hide;
    }

    /**
     * Get the used reduction type.
     *
     * @return The current reduction type.
     */
    public @NotNull ReductionType getReduction() {
        return red;
    }

    /**
     * Hide content in output.
     *
     * @return An instance of the same class with the hide tag set to true.
     * @see #withHideTag(boolean)
     */
    public @NotNull Rule enableHideTag() {
        return withHideTag(true);
    }

    /**
     * Unhide content in output.
     *
     * @return An instance of the same class with the hide tag set to false.
     * @see #withHideTag(boolean)
     */
    public @NotNull Rule unhideContent() {
        return withHideTag(false);
    }

    /**
     * Hide the tag associated with this rule.
     * Wrap this rule around the entire right-hand side.
     *
     * @return A new instance of the same class.
     */
    public @NotNull Rule hideTag() {
        return withReduction(ReductionType.standardIntermediateReduction());
    }

    // Force children to override this.
    @Override
    public abstract boolean equals(Object o);

    // Force children to override this.
    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return Print.ruleToString(this);
    }
}
