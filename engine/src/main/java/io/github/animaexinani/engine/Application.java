package io.github.animaexinani.engine;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.animaexinani.engine.windowing.WindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.SDLInit;

import io.github.animaexinani.engine.internal.EventDispatcher;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.internal.video.VideoSubsystem;
import io.github.animaexinani.engine.listeners.QuitEventListener;

public abstract class Application implements AutoCloseable, Runnable {
    private static final class State implements Runnable {
        @NotNull
        private final EventDispatcher eventDispatcher;        

        @NotNull
        public EventDispatcher eventDispatcher() {
            return this.eventDispatcher;
        }

        @Nullable
        private VideoSubsystem videoSubsystem;

        @NotNull
        public VideoSubsystem videoSubsystem() {
            if (this.videoSubsystem == null) {
                this.videoSubsystem = new VideoSubsystem();
            }

            return this.videoSubsystem;
        }

        private final AtomicBoolean cleaned;
        
        @Override
        public void run() {
            this.cleaned.setRelease(true);
            this.eventDispatcher.close();
            SDLInit.SDL_Quit();
        }

        public State() {
            this.eventDispatcher = new EventDispatcher();
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    private final AtomicBoolean running;

    @NotNull
    protected final EventRegistry eventRegistry() {
        return this.state.eventDispatcher();
    }

    @NotNull
    protected final WindowFactory windowFactory() {
        return this.state.videoSubsystem();
    }

    /**
     * A callback function that runs one iteration of the main loop.
     * @return Whether the application should continue running.
     * 
     * @implNote This function is guaranteed to run in the same thread that called {@link #run()}.
     */
    protected abstract boolean iterate();

    /**
     * Initializes the engine for the application.
     * 
     * @param options Options regarding the initialization metadata and hints of the application.
     * 
     * @apiNote This should be called on the same thread which called the entry point.
     */
    protected Application(@NotNull ApplicationOptions options) {
        Objects.requireNonNull(options);

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_NAME_STRING, options.getName())
            );
        }
        catch (SdlOperationFailedException e) {
            throw new MetadataInitializationException("Failed to set application name", e);
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_VERSION_STRING, options.getVersion())
            );
        }
        catch (SdlOperationFailedException e) {
            throw new MetadataInitializationException("Failed to set application version", e);
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_IDENTIFIER_STRING, options.getIdentifier())
            );
        }
        catch (SdlOperationFailedException e) {
            throw new MetadataInitializationException("Failed to set application identifier", e);
        }

        this.state = new State();
        this.cleanable = GlobalCleaner.register(this, this.state);

        this.running = new AtomicBoolean(false);
    }

    /**
     * Closes the application, releasing owned resources and uninitializing the engine.
     * 
     * @throws IllegalStateException if the application is still running
     */
    @Override
    public void close() {
        if (this.running.getAcquire()) {
            throw new IllegalStateException("Attempted to close a running application");
        }

        this.cleanable.clean();
    }

    /**
     * Runs the application.
     * 
     * @apiNote
     * This method will return only when the application finishes running.
     * @apiNote
     * This should be called on the same thread which created the application.
     * 
     * @throws IllegalStateException if the application is either closed or already running
     */
    @Override
    public void run() {
        if (this.state.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to run an already-closed application");
        }

        if (this.running.getAndSet(true)) {
            throw new IllegalStateException("Attempted to run an already-running application");
        }

        QuitEventListener listener = () -> running.setRelease(false);
        var eventDispatcher = this.state.eventDispatcher();
        eventDispatcher.register(QuitEventListener.class, listener);

        try {
            while (this.running.getAcquire()) {
                eventDispatcher.processEvents();
                if (!this.running.getAcquire()) {
                    break;
                }

                var result = iterate();
                if (!result) {
                    this.running.setRelease(false);
                }
            }
        }
        finally {
            eventDispatcher.remove(QuitEventListener.class, listener);
            this.running.setRelease(false);
        }
    }
}
