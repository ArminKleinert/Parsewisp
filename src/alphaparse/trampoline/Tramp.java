package alphaparse.trampoline;

import alphaparse.collections.IntMap;
import alphaparse.grammar.Grammar;
import alphaparse.functions.NegativeListener;
import alphaparse.functions.Procedure;
import alphaparse.parsing.StringTerm;
import alphaparse.result.AlphaParseFailure;
import alphaparse.result.success.AlphaParseMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static alphaparse.trampoline.TrampolineListenerNode.TrampolineListenerKey;

/**
 * This class carries the inner state during the parsing process. It should not be used directly.
 */
public final class Tramp {
    private final @NotNull Grammar grammar;
    private final @NotNull String text;
    private final int failIndex;
    private final @NotNull ArrayList<@NotNull Procedure> stack;
    private final @NotNull ArrayList<@NotNull Procedure> nextStack;
    private int generation;
    //private final @NotNull TreeMap<@NotNull Integer, @NotNull NegativeListener> negativeListeners;
    private final @NotNull IntMap<@NotNull NegativeListener> negativeListeners;
    private final @NotNull LinkedHashMap<@NotNull TrampolineMsgCacheKey, @NotNull Integer> msgCache;
    private final @NotNull LinkedHashMap<@NotNull TrampolineListenerKey, @NotNull TrampolineListenerNode> nodes;
    private @Nullable AlphaParseMessage success;
    private @Nullable AlphaParseFailure failure;

    /**
     * Normal constructor.
     *
     * @param grammar The grammar.
     * @param text    The text.
     */
    public Tramp(final @NotNull Grammar grammar, final @NotNull String text) {
        this(grammar, text, -1);
    }

    /**
     * Used to continue parsing after a failure.
     *
     * @param grammar   The grammar.
     * @param text      The text.
     * @param failIndex The index at which to continue.
     */
    public Tramp(final @NotNull Grammar grammar, final @NotNull String text, final int failIndex) {
        this.grammar = grammar;
        this.text = text;
        this.failIndex = failIndex;
        this.stack = new ArrayList<>();
        this.nextStack = new ArrayList<>();
        this.generation = 0;
        this.negativeListeners = new IntMap<>(1024);
        //this.negativeListeners = new LinkedHashMap<>();
        this.msgCache = new LinkedHashMap<>();
        this.nodes = new LinkedHashMap<>();
        this.success = null;
        this.failure = null;
    }

    /**
     * Get the grammar.
     *
     * @return The grammar.
     */
    public @NotNull Grammar getGrammar() {
        return grammar;
    }

    /**
     * Get the text.
     *
     * @return The text.
     */
    public @NotNull String getText() {
        return text;
    }

    /**
     * Get the index at which parsing started. This is only important if parsing had to be restarted after a failure.
     *
     * @return Get the index at which parsing started.
     */
    public int getFailIndex() {
        return failIndex;
    }

    /**
     * A stack of derivative parse procedures of the current generation.
     *
     * @return A stack of derivative parse procedures of the current generation.
     */
    public @NotNull ArrayList<@NotNull Procedure> getStack() {
        return stack;
    }

    /**
     * The current generation of derivatives.
     *
     * @return The current generation of derivatives.
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Sequential map of negative lookaheads.
     *
     * @return Sequential map of negative lookaheads.
     */
    public @NotNull IntMap<@NotNull NegativeListener> getNegativeListeners() {
        return negativeListeners;
    }

    /**
     * Removes and returns the last negative listener.
     *
     * @return The last negative listener or null if there were none.
     */
    public @Nullable NegativeListener pollAndRemovePreviousNegativeListener() {
        return negativeListeners.intMapPoll();
    }

    /**
     * Last registered success.
     *
     * @return Last registered success.
     */
    public @Nullable AlphaParseMessage getSuccess() {
        return success;
    }

    /**
     * If parsing failed, a failure is set.
     *
     * @return If parsing failed, a failure is set.
     */
    public @Nullable AlphaParseFailure getFailure() {
        return failure;
    }

    /**
     * Increments the generation.
     */
    public void nextGeneration() {
        generation = generation + 1;
        stack.clear();
        stack.addAll(nextStack);
        nextStack.clear();
    }

    /**
     * Adds a procedure to the stack.
     *
     * @param frame The frame.
     */
    public void addToStack(final @NotNull Procedure frame) {
        this.stack.add(frame);
    }

    /**
     * Removes the top of the stack.
     */
    public void popStack() {
        stack.remove(stack.size() - 1);
    }

    /**
     * Adds an element to the stack for the next generation.
     *
     * @param frame The next frame.
     */
    public void addToNextStack(final @NotNull Procedure frame) {
        nextStack.add(frame);
    }

    /**
     * Returns the "generation" a listener will belong to.
     *
     * @param key        The key for the message.
     * @param defaultVal The generation to use if the key was not registered.
     * @return The generation.
     */
    public int getFromMsgCache(final @NotNull TrampolineMsgCacheKey key, final int defaultVal) {
        return msgCache.getOrDefault(key, defaultVal);
    }

    /**
     * Adds a listener to the message cache.
     *
     * @param key        The key for the message.
     * @param defaultVal The generation the listener is for.
     */
    public void addToMsgCache(final @NotNull TrampolineMsgCacheKey key, final int defaultVal) {
        msgCache.put(key, defaultVal);
    }

    /**
     * This function is used when the parser detects a failure. Parsing will be terminated when a failure is set.
     *
     * @param failure The failure.
     */
    public void setFailure(final @NotNull AlphaParseFailure failure) {
        this.failure = failure;
    }

    /**
     * Sets the last detected parsing success. This is usually a String for a parsed {@link StringTerm} or a finished {@link alphaparse.result.ParseTree}.
     *
     * @param success The new success or null.
     */
    public void setSuccess(final @Nullable AlphaParseMessage success) {
        this.success = success;
    }

    /**
     * Gets a listener node (if registered) from the trampoline.
     *
     * @param nodeKey The key (position and production).
     * @return The node or null.
     */
    public @Nullable TrampolineListenerNode getNode(final @NotNull TrampolineListenerKey nodeKey) {
        return nodes.get(nodeKey);
    }

    /**
     * Adds a new listener-node.
     *
     * @param key  The key (position and production)
     * @param node The node.
     */
    public void addToNodes(final @NotNull TrampolineListenerKey key,
                           final @NotNull TrampolineListenerNode node) {
        nodes.put(key, node);
    }

    @Override
    public @NotNull String toString() {
        return "Tramp{" +
                "grammar=" + grammar +
                ", text='" + text + '\'' +
                ", failIndex=" + failIndex +
                ", stack=" + stack +
                ", nextStack=" + nextStack +
                ", generation=" + generation +
                ", negativeListeners=" + negativeListeners +
                ", msgCache=" + msgCache +
                ", nodes=" + nodes +
                ", success=" + success +
                ", failure=" + failure +
                '}';
    }
}
