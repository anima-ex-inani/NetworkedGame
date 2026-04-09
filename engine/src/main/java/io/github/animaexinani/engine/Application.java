package io.github.animaexinani.engine;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.animaexinani.engine.assets.AssetManager;
import io.github.animaexinani.engine.internal.assets.AssetManagerImpl;
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
    private static final class NativeState implements Runnable {
        private final AtomicBoolean cleaned;
        
        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLInit.SDL_Quit();
        }

        public NativeState() {
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private final @NotNull AssetManagerImpl assetManager = new AssetManagerImpl();
    private final @NotNull EventDispatcher eventDispatcher;

    private @Nullable VideoSubsystem videoSubsystem;
    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;

    private final AtomicBoolean running;

    @NotNull
    protected final EventRegistry eventRegistry() {
        return this.eventDispatcher;
    }

    @NotNull
    protected final WindowFactory windowFactory() {
        if (this.videoSubsystem == null) {
            this.videoSubsystem = new VideoSubsystem();
        }

        return this.videoSubsystem;
    }

    protected final @NotNull AssetManager assetManager() {
        return this.assetManager;
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
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_NAME_STRING, options.name())
            );
        }
        catch (SdlOperationFailedException e) {
            throw new MetadataInitializationException("Failed to set application name", e);
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_VERSION_STRING, options.version())
            );
        }
        catch (SdlOperationFailedException e) {
            throw new MetadataInitializationException("Failed to set application version", e);
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_IDENTIFIER_STRING, options.identifier())
            );
        }
        catch (SdlOperationFailedException e) {
            throw new MetadataInitializationException("Failed to set application identifier", e);
        }

        if (Objects.nonNull(options.creator())) {
            try {
                SdlOperationFailedException.throwOnFailure(
                    SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_CREATOR_STRING, options.creator())
                );
            }
            catch (SdlOperationFailedException e) {
                throw new MetadataInitializationException("Failed to set application creator", e);
            }
        }

        if (Objects.nonNull(options.copyright())) {
            try {
                SdlOperationFailedException.throwOnFailure(
                    SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_COPYRIGHT_STRING, options.copyright())
                );
            }
            catch (SdlOperationFailedException e) {
                throw new MetadataInitializationException("Failed to set application copyright", e);
            }
        }

        if (Objects.nonNull(options.url())) {
            try {
                SdlOperationFailedException.throwOnFailure(
                    SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_URL_STRING, options.url())
                );
            }
            catch (SdlOperationFailedException e) {
                throw new MetadataInitializationException("Failed to set application URL", e);
            }
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_TYPE_STRING, "game")
            );
        }
        catch (SdlOperationFailedException e) {
            throw new MetadataInitializationException("Failed to set application type", e);
        }

        this.nativeState = new NativeState();
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
        this.eventDispatcher = new EventDispatcher();

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

        if (this.videoSubsystem != null) {
            this.videoSubsystem.close();
        }
        this.eventDispatcher.close();
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
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to run an already-closed application");
        }

        if (this.running.getAndSet(true)) {
            throw new IllegalStateException("Attempted to run an already-running application");
        }

        QuitEventListener listener = () -> this.running.setRelease(false);
        this.eventDispatcher.register(QuitEventListener.class, listener);

        try {
            while (this.running.getAcquire()) {
                this.eventDispatcher.processEvents();
                if (!this.running.getAcquire()) {
                    break;
                }

                var result = this.iterate();
                if (!result) {
                    this.running.setRelease(false);
                }
            }
        }
        finally {
            this.eventDispatcher.remove(QuitEventListener.class, listener);
            this.running.setRelease(false);
        }
    }
}
