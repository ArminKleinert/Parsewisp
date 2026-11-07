package de.kleinert.parsewisp.trampoline;

import de.kleinert.parsewisp.parsing.Rule;
import org.jetbrains.annotations.NotNull;

/**
 * These objects are used to index {@link ListenerNode} objects in a {@link Tramp}. They ensure that no productions are ever repeated at a specified index.
 *
 * @param index The index.
 * @param rule  The production.
 * @since 0.9.7
 */
public record ListenerKey(int index, @NotNull Rule rule) {
}
