package de.kleinert.parsewisp;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class for string-alternatives that are slightly slower to create than strings, but provide guaranteed
 * interning and O(1) comparisons via the build-in {@code ==} operator.
 * <p>
 * {@code
 * Sym k1 = Sym.sym(str);
 * Sym k2 = Sym.sym(str);
 * k1 == k2 // Guarantied to be true.
 * }
 */
public final class Sym {
    private static final @NotNull Map<@NotNull String, Reference<Sym>> table =
            new ConcurrentHashMap<>();
    private static final @NotNull ReferenceQueue<@NotNull Sym> rq =
            new ReferenceQueue<>();

    private final @NotNull String name;

    /**
     * Creates a new symbol or returns an existing one.
     *
     * @param sym The string for the symbol.
     * @return The new or already existing symbol.
     */
    public static @NotNull Sym sym(final @NotNull String sym) {
        Sym k = null;
        var existingRef = table.get(sym);
        if (existingRef == null) {
            if (rq.poll() != null) {
                Object o = rq.poll();
                while (o != null) {
                    o = rq.poll();
                }

                for (final @NotNull var e : table.entrySet()) {
                    final @NotNull var val = e.getValue();
                    if (val.get() == null) {
                        table.remove(e.getKey(), val);
                    }
                }
            }

            k = new Sym(sym);
            existingRef = table.putIfAbsent(sym, new WeakReference<>(k, rq));
        }

        if (existingRef == null) {
            return k;
        } else {
            var existing = existingRef.get();
            if (existing != null) {
                return existing;
            }
            table.remove(sym, existingRef);
            return sym(sym);
        }
    }

    private Sym(final @NotNull String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() - 1640531527;
    }

    @Override
    public @NotNull String toString() {
        return ":" + this.name;
    }

    /**
     * Returns the backing string.
     *
     * @return The backing string.
     */
    public @NotNull String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
