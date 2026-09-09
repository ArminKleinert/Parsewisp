package de.kleinert.parsewisp.parsing;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.collections.FlatResultSeq;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.functions.Listener;
import de.kleinert.parsewisp.functions.NegativeListener;
import de.kleinert.parsewisp.functions.Procedure;
import de.kleinert.parsewisp.reduction.ReductionType;
import de.kleinert.parsewisp.result.*;
import de.kleinert.parsewisp.result.failure.FailureUtil;
import de.kleinert.parsewisp.result.success.ParseMessage;
import de.kleinert.parsewisp.result.failure.ParseFailureReason;
import de.kleinert.parsewisp.trampoline.TrampolineMsgCacheKey;
import de.kleinert.parsewisp.trampoline.TrampolineListenerNode;

import static de.kleinert.parsewisp.trampoline.TrampolineListenerNode.TrampolineListenerKey;

import de.kleinert.parsewisp.trampoline.Tramp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class provides the general parsing algorithms.
 */
public final class Gll {
    private final @NotNull Tramp tramp;
    private final boolean iterativeDeepening;

    Tramp tramp() {
        return tramp;
    }

    boolean iterativeDeepening() {
        return iterativeDeepening;
    }

    private Gll(final @NotNull Tramp tramp, final boolean iterativeDeepening) {
        this.tramp = tramp;
        this.iterativeDeepening = iterativeDeepening;
    }

    private @NotNull TrampolineListenerNode getOrCreateListenerNode(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey) {
        @Nullable TrampolineListenerNode node = tramp.getNode(nodeKey);

        if (node != null)
            return node;

        node = new TrampolineListenerNode();
        tramp.addToNodes(nodeKey, node);
        return node;
    }

    private boolean listenerExists_Q(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey) {
        TrampolineListenerNode node = tramp.getNode(nodeKey);
        if (node == null) return false;
        return !node.listeners().isEmpty();
    }

    private boolean fullListenerExists_Q(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey) {
        final TrampolineListenerNode node = tramp.getNode(nodeKey);
        if (node == null) return false;
        return !node.listeners().isEmpty() || !node.fullListeners().isEmpty();
    }

    void pushNegativeListener(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey creator,
            final @NotNull NegativeListener negativeListener) {
        tramp.getNegativeListeners().put(creator.index(), negativeListener);
    }

    private void pushMessage(
            final @NotNull Listener listener,
            final @NotNull ParseMessage result) {
        final int i = result.index();
        final TrampolineMsgCacheKey k = new TrampolineMsgCacheKey(i, listener);
        final int c = tramp.getFromMsgCache(k, 0);
        final Procedure f = () -> listener.execute(result);
        if (c > tramp.getGeneration()) {
            tramp.addToNextStack(f);
        } else {
            tramp.addToStack(f);
        }
        tramp.addToMsgCache(k, c + 1);
    }

    private void pushStack(final @NotNull Procedure item) {
        tramp.addToStack(item);
    }

    private void step() {
        final Procedure top = tramp.getStack().get(tramp.getStack().size() - 1);
        tramp.popStack();
        top.execute();
    }

    private @NotNull ParsesResult.LazyResultList run() {
        return run(Integer.MAX_VALUE);
    }

    private @NotNull ParsesResult.LazyResultList run(
            final int maxResults) {
        final var foundResult = new AtomicBoolean(false);
        return new ParsesResult.LazyResultList((i) -> run(foundResult), maxResults);
    }

    private @Nullable ParseTree run(
            final @NotNull AtomicBoolean foundResult) {
        for (; ; ) {
            if (tramp.getSuccess() != null) {
                final @NotNull var successResult = tramp.getSuccess();
                final var resultTree = successResult.getResult();
                if (!(resultTree instanceof ParseTree))
                    throw new IllegalStateException(successResult + " in instance " + getInstanceIdForDebug());
                tramp.setSuccess(null);
                foundResult.set(true);
                return (ParseTree) resultTree;
            }
            final @NotNull List<@NotNull Procedure> stack = tramp.getStack();
            if (!stack.isEmpty()) {
                step();
                continue; // Take it to the top.
            }
            var lastNegativeListener = tramp.pollAndRemovePreviousNegativeListener();
            if (lastNegativeListener != null) {
                lastNegativeListener.execute();
                continue; // Take it to the top.
            }
            if (foundResult.get()) {
                tramp.nextGeneration();
                foundResult.set(false);
                continue; // Take it to the top.
            }
            return null; // Fail
        }
    }

    void pushListener(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final @NotNull Listener listener) {
        final boolean listenerAlreadyExists = listenerExists_Q(nodeKey);
        final @NotNull TrampolineListenerNode node = getOrCreateListenerNode(nodeKey);
        final @NotNull List<Listener> listeners = node.listeners();
        listeners.add(listener);
        for (final @NotNull ParseMessage result : node.results()) {
            pushMessage(listener, result);
        }
        for (final @NotNull ParseMessage fullResult : node.fullResults()) {
            pushMessage(listener, fullResult);
        }
        if (!listenerAlreadyExists) {
            pushStack(() -> nodeKey.parser().parse(nodeKey.index(), this));
        }
    }

    void pushFullListener(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final @NotNull Listener listener) {
        //GllParsers.pushFullListenerCallback.invoke(tramp, nodeKey, listener);
        final var fullListenerAlreadyExists = fullListenerExists_Q(nodeKey);
        final @NotNull var node = getOrCreateListenerNode(nodeKey);
        final @NotNull var listeners = node.fullListeners();
        listeners.add(listener);
        for (final @NotNull ParseMessage fullResult : node.fullResults()) {
            pushMessage(listener, fullResult);
        }
        if (!fullListenerAlreadyExists) {
            pushStack(() -> nodeKey.parser().fullParse(nodeKey.index(), this));
        }
    }

    /**
     * Pushes a result into the trampoline's node.
     * Categorizes as either result or full result.
     * Schedules notification to all existing listeners of result.
     * (Full listeners only get notified about full results)
     */
    private void pushResultHelper(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            @NotNull ParseMessage result) {
        final @NotNull TrampolineListenerNode node = getOrCreateListenerNode(nodeKey);
        final @NotNull Rule parser = nodeKey.parser();
        if (parser.isHidden()) {
            result = result.reset();
        }
        if (parser.getReduction().getReductionType() != ReductionType.ReductionTypesAvailable.INITIAL) {
            final ParseTree tree = ParseTree.create(
                    parser.getReduction().getKey(), result.getResult(),
                    nodeKey.index(), result.index());
            result = ParseMessage.create(result.index(), tree);
        }

        final boolean reachedEndOfInput = tramp.getText().length() == result.index();
        final @NotNull LinkedHashSet<@NotNull ParseMessage> results =
                reachedEndOfInput ? node.fullResults() : node.results();

        final var resultExisted = !results.add(result);
        if (resultExisted) {
            return;
        }

        for (final @NotNull Listener listener : node.listeners()) {
            pushMessage(listener, result);
        }

        if (!reachedEndOfInput) {
            return;
        }

        for (final @NotNull Listener fullListener : node.fullListeners()) {
            pushMessage(fullListener, result);
        }
    }

    private void startParser(
            final @NotNull Tramp tramp,
            final @NotNull Rule parser,
            final boolean partial) {
        if (partial) {
            pushListener(new TrampolineListenerKey(0, parser), tramp::setSuccess);
        } else {
            pushFullListener(new TrampolineListenerKey(0, parser), tramp::setSuccess);
        }
    }

    void pushErrorMessage(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final @NotNull ParseFailureNode result,
            final int end) {
        pushResultHelper(nodeKey, ParseMessage.create(end, result));
    }

    void pushSuccessMessage(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final @NotNull String result,
            final int end) {
        final @NotNull ParseMessage aps = ParseMessage.create(end, result);
        pushResultHelper(nodeKey, aps);
    }

    void pushSuccessMessageWithoutValue(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final int end) {
        pushResultHelper(nodeKey, ParseMessage.create(end));
    }

    void pushSuccessMessage(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final @NotNull FlatResultSeq result,
            final int end) {
        final @NotNull ParseMessage aps = ParseMessage.create(end, result);
        pushResultHelper(nodeKey, aps);
    }

    void pushSuccessAgainWithNewKey(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final @NotNull ParseMessage message
    ) {
        pushResultHelper(nodeKey, message);
    }

    void fail(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
            final int index,
            final @NotNull ParseFailureReason reason) {
        //Objects.requireNonNull(tramp.getFailure());
        tramp.setFailure(FailureUtil.modifyFailureByIndex(tramp.getFailure(), reason, index));
        if (index == tramp.getFailIndex()) {
            final @NotNull String subSeq = tramp.getText().substring(index);
            final int textLen = tramp.getText().length();
            pushErrorMessage(
                    nodeKey,
                    buildFailureNode(Sym.sym("failure"), subSeq, index, tramp.getText().length()),
                    textLen);
        }
    }

    private @NotNull ParseFailureNode buildFailureNode(
            final @NotNull Sym key,
            final @NotNull String text,
            final int start,
            final int end) {
        return new ParseFailureNode(text, key, start, end);
    }

    private static @NotNull ParsesResult parsesEmbedFailureAfterFail(
            final @NotNull Grammar grammar,
            final @NotNull Sym start,
            final @NotNull String text,
            final boolean partial,
            final boolean iterativeDeepening) {
        final @NotNull var tramp = new Tramp(grammar, text, 0);
        final @NotNull var parser = NonTerminal.create(start);
        var gll = new Gll(tramp, iterativeDeepening);
        gll.startParser(tramp, parser, partial);
        final @NotNull var allParses = gll.run();
        return ParsesResult.make(allParses);
    }

    @NotNull Listener nodeListener(
            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey) {
        return result -> pushResultHelper(nodeKey, result);
    }

    /**
     * This method should not be called directly. Use {@link Parser#parses(String)} or {@link Parser#parses(String, ParsingOptions)} instead.
     *
     * @param grammar            The grammar.
     * @param start              The name of the start production.
     * @param text               The text.
     * @param partial            Whether to include partial results.
     * @param iterativeDeepening Iteratively deepens the evaluation of {@link RegexTerm#parse}.
     * @param errorIfEmpty       If true, return an error if the parsing failed. By default, an empty list would be returned.
     * @return The parse forest.
     * @see Parser#parses(String)
     * @see Parser#parses(String, ParsingOptions)
     */
    public static @NotNull ParsesResult parses(
            final @NotNull Grammar grammar,
            final @NotNull Sym start,
            final @NotNull String text,
            final boolean partial,
            final boolean iterativeDeepening,
            final boolean errorIfEmpty) {
        final @NotNull var tramp = new Tramp(grammar, text);
        final @NotNull var parser = NonTerminal.create(start);
        final @NotNull var gll = new Gll(tramp, iterativeDeepening);
        gll.startParser(tramp, parser, partial);
        final @NotNull var allParses = gll.run();
        if (errorIfEmpty && allParses.isEmpty()) {
            if (tramp.getFailure() == null)
                throw new IllegalStateException("Parsing failed. Perhaps the grammar was non-productive?");
            @NotNull ParseFailure apf = FailureUtil.augmentFailure(tramp.getFailure(), text);
            return ParsesResult.make(apf);
        }
        return ParsesResult.make(allParses);
    }

    /**
     * This method should not be called directly. Use {@link Parser#parse(String)} or {@link Parser#parse(String, ParsingOptions)} instead.
     *
     * @param grammar            The grammar.
     * @param start              The name of the start production.
     * @param text               The text.
     * @param partial            Whether to include partial results.
     * @param iterativeDeepening Iteratively deepens the evaluation of {@link RegexTerm#parse}.
     * @return The parse tree or failure.
     * @see Parser#parse(String)
     * @see Parser#parse(String, ParsingOptions)
     */
    public static @NotNull ParseResult parse(
            final @NotNull Grammar grammar,
            final @NotNull Sym start,
            final @NotNull String text,
            final boolean partial,
            final boolean iterativeDeepening) {
        final @NotNull var tramp = new Tramp(grammar, text);
        final @NotNull var gll = new Gll(tramp, iterativeDeepening);
        final @NotNull var parser = NonTerminal.create(start);
        gll.startParser(tramp, parser, partial);
        final @NotNull var allParses = gll.run(1);
        if (allParses.isEmpty()) {
            if (tramp.getFailure() == null)
                throw new IllegalStateException("Parsing failed. Perhaps the grammar was non-productive?");
            return ParseResult.make(FailureUtil.augmentFailure(tramp.getFailure(), text));
        }
        return ParseResult.make(allParses.getFirst());
    }

    /**
     * This method should not be called directly. Use {@link Parsewisp#parses(Parser, String, ParsingOptions)} with {@link ParsingOptions#embedFailureInParseTree()} set to true instead.
     *
     * @param grammar            The grammar.
     * @param start              The name of the start production.
     * @param text               The text.
     * @param partial            Whether to include partial results.
     * @param iterativeDeepening Iteratively deepens the evaluation of {@link RegexTerm#parse}.
     * @return The parse forest.
     * @see Parsewisp#parses(Parser, String, ParsingOptions)
     * @see ParsingOptions#embedFailureInParseTree()
     */
    public static @NotNull ParsesResult parsesEmbedFailure(
            final @NotNull Grammar grammar,
            final @NotNull Sym start,
            final @NotNull String text,
            final boolean partial,
            final boolean iterativeDeepening) {
        final @NotNull var allParses = parses(grammar, start, text, partial, iterativeDeepening, false);
        if (!allParses.castToParsesSuccess().isEmpty()) return ParsesResult.make(allParses);
        return parsesEmbedFailureAfterFail(grammar, start, text, partial, iterativeDeepening);
    }

    private static @NotNull ParseResult parseEmbedFailureAfterFail(
            final @NotNull Grammar grammar,
            final @NotNull Sym start,
            final @NotNull String text,
            final int failIndex,
            final boolean partial,
            final boolean iterativeDeepening) {
        final @NotNull var tramp = new Tramp(grammar, text, failIndex);
        final @NotNull var parser = NonTerminal.create(start);
        final @NotNull var gll = new Gll(tramp, iterativeDeepening);
        gll.startParser(tramp, parser, partial);
        final @NotNull var allParses = gll.run(1);
        if (!allParses.isEmpty())
            return ParseResult.make(allParses.getFirst());
        return gll.buildFailureNode(start, text, 0, text.length());
    }

    /**
     * This method should not be called directly. Use {@link Parsewisp#parse(Parser, String, ParsingOptions)} with {@link ParsingOptions#embedFailureInParseTree()} set to true instead.
     *
     * @param grammar            The grammar.
     * @param start              The name of the start production.
     * @param text               The text.
     * @param partial            Whether to include partial results.
     * @param iterativeDeepening Iteratively deepens the evaluation of {@link RegexTerm#parse}.
     * @return The parse tree or failure.
     * @see Parsewisp#parse(Parser, String, ParsingOptions)
     * @see ParsingOptions#embedFailureInParseTree()
     */
    public static @NotNull ParseResult parseEmbedFailure(
            final @NotNull Grammar grammar,
            final @NotNull Sym start,
            final @NotNull String text,
            final boolean partial,
            final boolean iterativeDeepening) {
        final @NotNull var result = parse(grammar, start, text, partial, iterativeDeepening);
        if (!(result instanceof ParseFailure)) return result;
        return parseEmbedFailureAfterFail(grammar, start, text, ((ParseFailure) result).index(), partial, iterativeDeepening);
    }

    private String getInstanceIdForDebug() {
        //noinspection RedundantCast
        return ((Object) this).toString();
    }
}
