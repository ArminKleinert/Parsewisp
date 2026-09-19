package de.kleinert.parsewisp.trampoline;

import de.kleinert.parsewisp.functions.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * A key for the trampoline's message cache.
 *
 * @param index    The index.
 * @param listener The listener.
 * @since 0.9.7
 */
public record TrampolineMsgCacheKey(int index, @NotNull Listener listener) {
}
