package io.github.animaexinani.engine.internal;

import java.lang.ref.Cleaner;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDL_Event;

import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.listeners.*;

public final class EventDispatcher implements EventRegistry, AutoCloseable {
    private final Map<Class<? extends EventListener>, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private static final List<EventListener> NO_LISTENERS = List.of();

    private static final class State implements Runnable {
        @NotNull
        private final SDL_Event event;

        @Override
        public void run() {
            event.close();
            SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_EVENTS);
        }

        public State() {
            SdlOperationFailedException.throwOnFailure(SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_EVENTS));
            this.event = SDL_Event.create();
        }
    }

    @NotNull
    private final State state;

    @NotNull
    private final Cleaner.Cleanable cleanable;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EventListener> void register(@NotNull Class<T> type, @NotNull T listener) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(listener);

        var listeners1 = (List<T>)this.listeners.computeIfAbsent(type, _ -> new CopyOnWriteArrayList<>());
        listeners1.add(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EventListener> void remove(@NotNull Class<T> type, @NotNull T listener) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(listener);

        var listeners1 = (List<T>)this.listeners.get(type);
        if (listeners1 != null) {
            listeners1.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends EventListener> List<T> getListenersOfType(Class<T> type) {
        return (List<T>)this.listeners.getOrDefault(type, NO_LISTENERS);
    }

    public void processEvents() {
        while (SDLEvents.SDL_PollEvent(this.state.event)) {
            switch (this.state.event.type()) {
                case SDLEvents.SDL_EVENT_QUIT -> {
                    var quitListeners = getListenersOfType(QuitEventListener.class);
                    quitListeners.forEach(QuitEventListener::onQuit);
                }
                default -> {
                    // Intentionally empty.
                }
            }
        }
    }

    public void forceClose() {
        var quitListeners = getListenersOfType(QuitEventListener.class);
        quitListeners.forEach(QuitEventListener::onQuit);
    }

    public EventDispatcher() {
        this.state = new State();
        this.cleanable = GlobalCleaner.register(this, this.state);
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }
}
