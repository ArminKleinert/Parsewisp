package de.kleinert.parsewisp.parsing;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.reduction.ReductionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This type represents non-terminals.
 */
public final class NonTerminal extends SimpleRule {
    private final @NotNull Sym keyword;

    private NonTerminal(final boolean hide,
                        final @NotNull ReductionType red,
                        final @NotNull Sym keyword) {
        super(hide, red);
        this.keyword = keyword;
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering.
     * @param keyword The wrapped symbol.
     * @return A non-terminal.
     */
    public static @NotNull NonTerminal create(final @NotNull Sym keyword) {
        return new NonTerminal(defaultHidden, defaultReductionType, keyword);
    }

    /**
     * Returns the name.
     *
     * @return The name.
     */
    public @NotNull Sym getKeyword() {
        return keyword;
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @Nullable Rule rule = runner.tramp().getGrammar().getProduction(this.getKeyword());
        if (rule == null)
            throw new IllegalStateException("Cannot use non terminal "+this.getKeyword()+ " Availability should be checked when initializing parser.");
        runner.pushListener(
                new TrampolineListenerKey(index, rule),
                runner.nodeListener(new TrampolineListenerKey(index, this))
        );
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @Nullable Rule rule = runner.tramp().getGrammar().getProduction(this.getKeyword());
        if (rule == null)
            throw new IllegalStateException("Cannot use non terminal "+this.getKeyword()+ " Availability should be checked when initializing parser.");
        runner.pushFullListener(
                new TrampolineListenerKey(index, rule),
                runner.nodeListener(new TrampolineListenerKey(index, this)));
    }

    @Override
    public @NotNull NonTerminal withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new NonTerminal(hide, red, keyword);
    }

    @Override
    public @NotNull NonTerminal withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new NonTerminal(hide, red, keyword);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NonTerminal that)) return false;
        if (this == that) return true;
        return hide == that.hide && Objects.equals(red, that.red) && Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, red, keyword);
    }
}
