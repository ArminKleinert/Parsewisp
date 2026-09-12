package de.kleinert.parsewisp.trampoline;

import de.kleinert.parsewisp.functions.Listener;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.result.success.ParseMessage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * These objects save listeners and results for a specific index and rule to ensure that no productions are ever repeated for a specific index.
 *
 * @param listeners     Listener objects for parses.
 * @param fullListeners Listeners that expect to be parsed to the end of the input text.
 * @param results       Successes (or failures) for listeners.
 * @param fullResults   Successes (or failures) for full listeners.
 */
public record TrampolineListenerNode(@NotNull List<Listener> listeners,
                                     @NotNull List<Listener> fullListeners,
                                     @NotNull LinkedHashSet<ParseMessage> results,
                                     @NotNull LinkedHashSet<ParseMessage> fullResults) {
    /**
     * Creates a new instance.
     */
    public TrampolineListenerNode() {
        this(new ArrayList<>(), new ArrayList<>(), new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    /**
     * These objects are used to index {@link TrampolineListenerNode} objects in a {@link Tramp}. They ensure that no productions are ever repeated at a specified index.
     *
     * @param index The index.
     * @param rule  The production.
     */
    public record TrampolineListenerKey(int index, @NotNull Rule rule) {
    }
}
