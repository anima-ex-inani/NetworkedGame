package io.github.animaexinani.engine;

import java.util.EventListener;

import org.jetbrains.annotations.NotNull;

/**
 * An interface to a registry of event listeners.
 */
public interface EventRegistry {
    <T extends EventListener> void register(@NotNull Class<T> type, @NotNull T listener);

    <T extends EventListener> void remove(@NotNull Class<T> type, @NotNull T listener);
}
