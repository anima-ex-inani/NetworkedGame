package io.github.animaexinani.engine.internal.video;

import java.lang.ref.Cleaner.Cleanable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLRender;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_MainThreadCallback;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.RenderingOperationFailedException;
import io.github.animaexinani.engine.windowing.Window;

public final class WindowWithRenderer implements Window, Renderer {
    private static final class State implements Runnable {
        private final long windowHandle;
        private final long rendererHandle;
        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            var cleanCallback = SDL_MainThreadCallback.create(_ -> {
                SDLRender.SDL_DestroyRenderer(this.rendererHandle);
                SDLVideo.SDL_DestroyWindow(this.windowHandle);
            });

            this.cleaned.setRelease(true);
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_RunOnMainThread(cleanCallback, 0, true)
            );
        }

        public State(long windowHandle, long rendererHandle) {
            this.windowHandle = windowHandle;
            this.rendererHandle = rendererHandle;
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private final State state;
    private final Cleanable cleanable;

    @Override
    public void clear(Color color) {
        float red = color.getRed();
        float green = color.getGreen();
        float blue = color.getBlue();
        float alpha = color.getAlpha();

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLRender.SDL_SetRenderDrawColorFloat(this.state.rendererHandle, red, green, blue, alpha)
            );
        }
        catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to set clear color", e);
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                SDLRender.SDL_RenderClear(this.state.rendererHandle)
            );
        }
        catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to clear back buffer", e);
        }
    }

    @Override
    public void present() {
        try {
            SdlOperationFailedException.throwOnFailure(
                SDLRender.SDL_RenderPresent(this.state.rendererHandle)
            );
        }
        catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to present back buffer", e);
        }
    }

    @Override
    public Renderer getRenderer() {
        return this;
    }

    public WindowWithRenderer(long windowHandle, long rendererHandle) {
        this.state = new State(windowHandle, rendererHandle);
        this.cleanable = GlobalCleaner.register(this, this.state);
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }
}
