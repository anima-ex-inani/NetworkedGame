package io.github.animaexinani.engine.internal;

import java.lang.ref.Cleaner;

import org.jetbrains.annotations.NotNull;

public final class GlobalCleaner {
    private static final Cleaner CLEANER = Cleaner.create();

    public static @NotNull Cleaner.Cleanable register(@NotNull Object obj, @NotNull Runnable action) {
        return CLEANER.register(obj, action);
    }

    private GlobalCleaner() {
    }
}
