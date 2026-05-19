package io.github.animaexinani.engine.internal;

import java.lang.ref.Cleaner;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Event;

import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.listeners.MouseDownListener;
import io.github.animaexinani.engine.listeners.MouseMoveListener;
import io.github.animaexinani.engine.listeners.MouseUpListener;
import io.github.animaexinani.engine.listeners.QuitEventListener;
import io.github.animaexinani.engine.listeners.TextInputListener;
import io.github.animaexinani.engine.events.KeyEvent;

public final class EventDispatcher implements EventRegistry, AutoCloseable {
    private final Map<Class<? extends EventListener>, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private static final List<EventListener> NO_LISTENERS = List.of();

    private static final class NativeState implements Runnable {
        @NotNull
        private final SDL_Event event;

        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            this.cleaned.setRelease(true);
            this.event.close();
            SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_EVENTS);
        }

        public NativeState() {
            SdlOperationFailedException.throwOnFailure(SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_EVENTS));
            this.event = SDL_Event.create();
            this.cleaned = new AtomicBoolean(false);
        }
    }

    @NotNull
    private final EventDispatcher.NativeState nativeState;

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
        return (List<T>)this.listeners.getOrDefault(type, EventDispatcher.NO_LISTENERS);
    }

    public void processEvents() {
        while (SDLEvents.SDL_PollEvent(this.nativeState.event)) {
            switch (this.nativeState.event.type()) {
                case SDLEvents.SDL_EVENT_QUIT -> {
                    var quitListeners = this.getListenersOfType(QuitEventListener.class);
                    quitListeners.forEach(QuitEventListener::onQuit);
                }
                case SDLEvents.SDL_EVENT_KEY_DOWN -> {
                    var keyListeners = this.getListenersOfType(KeyboardListener.class);
                    
                    // 1. Get the data from SDL
                    int scancode = this.nativeState.event.key().scancode();
                    boolean isRepeat = this.nativeState.event.key().repeat();
                    
                    // 2. Determine if it's a new press or a held repeat
                    var action = isRepeat ? KeyEvent.Action.REPEAT : KeyEvent.Action.PRESS;
                    
                    // 3. Package it into our clean Event Object and send it out!
                    var keyEvent = new KeyEvent(scancode, action);
                    keyListeners.forEach(l -> l.onKeyEvent(keyEvent));
                }
                case SDLEvents.SDL_EVENT_KEY_UP -> {
                    var keyListeners = this.getListenersOfType(KeyboardListener.class);
                    
                    // 1. Get the data from SDL
                    int scancode = this.nativeState.event.key().scancode();
                    
                    // 2. Package it as a RELEASE event and send it out!
                    var keyEvent = new KeyEvent(scancode, KeyEvent.Action.RELEASE);
                    keyListeners.forEach(l -> l.onKeyEvent(keyEvent));
                }
                case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN -> {
                    var mouseDownListeners = this.getListenersOfType(MouseDownListener.class);
                    var buttonEvent = this.nativeState.event.button();
                    mouseDownListeners.forEach(l -> l.onMouseDown(buttonEvent.button(), buttonEvent.x(), buttonEvent.y()));
                }
                case SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> {
                    var mouseUpListeners = this.getListenersOfType(MouseUpListener.class);
                    var buttonEvent = this.nativeState.event.button();
                    mouseUpListeners.forEach(l -> l.onMouseUp(buttonEvent.button(), buttonEvent.x(), buttonEvent.y()));
                }
                case SDLEvents.SDL_EVENT_MOUSE_MOTION -> {
                    var mouseMoveListeners = this.getListenersOfType(MouseMoveListener.class);
                    var motionEvent = this.nativeState.event.motion();
                    mouseMoveListeners.forEach(l -> l.onMouseMove(motionEvent.x(), motionEvent.y()));
                }
                case SDLEvents.SDL_EVENT_TEXT_INPUT -> {
                    var textInputListeners = this.getListenersOfType(TextInputListener.class);
                    var textInputEvent = this.nativeState.event.text();
                    textInputListeners.forEach(l -> l.onTextInput(textInputEvent.textString()));
                }

                default -> {
                    // Intentionally empty.
                }
            }
        }
    }

    public void forceClose() {
        var quitListeners = this.getListenersOfType(QuitEventListener.class);
        quitListeners.forEach(QuitEventListener::onQuit);
    }

    public EventDispatcher() {
        this.nativeState = new NativeState();
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }
}
