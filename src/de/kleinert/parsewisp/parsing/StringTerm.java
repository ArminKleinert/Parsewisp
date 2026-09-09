package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents string terminals (both case-sensitive or case-insensitive).
 * <br/>
 * Syntax: can be written with double quotes or single quotes, so {@code "..."} and {@code '...'} are equivalent.
 */
public final class StringTerm extends Terminal {
    private final @NotNull String string;
    private final boolean caseInsensitive;

    private StringTerm(final boolean hide,
                       final @NotNull ReductionType red,
                       final @NotNull String string,
                       final boolean caseInsensitive) {
        super(hide, red);
        this.string = string;
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
     *
     * @param string          The string to match.
     * @param caseInsensitive True if the casing doesn't matter, false if it does matter.
     * @return A rule.
     */
    public static @NotNull Rule create(final @NotNull String string, final boolean caseInsensitive) {
        if (string.isEmpty())
            return EpsilonTerm.getDefault();
        return new StringTerm(defaultHidden, defaultReductionType, string, caseInsensitive);
    }

    @Override
    public void parse(final int index, final @NotNull Gll runner) {
        final @NotNull String string = getString();
        final @NotNull String text = runner.tramp().getText();
        final int end = Integer.min(text.length(), index + string.length());
        final @NotNull String head = text.substring(index, end);

        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, this);
        if (caseInsensitive ? string.equalsIgnoreCase(head) : string.equals(head)) {
            runner.pushSuccessMessage(nodeKey, string, end);
        } else {
            runner.fail(nodeKey, index, ParseFailureReason.ofStringTerminal(this, false));
        }
    }

    @Override
    public void fullParse(final int index, final @NotNull Gll runner) {
        final @NotNull var string = getString();
        final @NotNull var text = runner.tramp().getText();
        final var end = Integer.min(text.length(), string.length() + index);
        final @NotNull var head = text.substring(index, end);
        final @NotNull TrampolineListenerKey nodeKey = new TrampolineListenerKey(index, this);
        if (text.length() == end && (caseInsensitive ? string.equalsIgnoreCase(head) : string.equals(head))) {
            runner.pushSuccessMessage(nodeKey, string, end);
        } else {
            runner.fail(nodeKey, index, ParseFailureReason.ofStringTerminal(this, true));
        }
    }

    /**
     * Returns the string.
     *
     * @return The string.
     */
    public @NotNull String getString() {
        return string;
    }

    /**
     * True if the casing doesn't matter, false if it does matter.
     *
     * @return True if the casing doesn't matter, false if it does matter.
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    @Override
    public @NotNull StringTerm withHideTag(final boolean hide) {
        return isHidden() == hide ? this : new StringTerm(hide, red, string, caseInsensitive);
    }

    @Override
    public @NotNull StringTerm withReduction(final @NotNull ReductionType red) {
        return getReduction() == red ? this : new StringTerm(hide, red, string, caseInsensitive);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StringTerm that)) return false;
        if (this == that) return true;
        return hide == that.hide
                && Objects.equals(red, that.red)
                && Objects.equals(string, that.getString())
                && caseInsensitive == that.caseInsensitive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hide, red, string, caseInsensitive);
    }
}
