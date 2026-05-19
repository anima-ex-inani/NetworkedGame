package io.github.animaexinani.engine.internal.video;

import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.internal.render.GPURenderer;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.windowing.Window;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLRender;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_MainThreadCallback;
import org.lwjgl.system.MemoryStack;

import java.lang.ref.Cleaner;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NativeWindow implements Window {
    private static final class NativeState implements Runnable {
        private static final SDL_MainThreadCallback cleanCallback = SDL_MainThreadCallback.create(
            SDLVideo::SDL_DestroyWindow
        );

        private final long handle;
        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLInit.SDL_RunOnMainThread(NativeState.cleanCallback, this.handle, true);
        }

        public NativeState(long handle) {
            this.handle = handle;
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private volatile @Nullable GPURenderer renderer;

    private @NotNull NativeState nativeState;

    private @NotNull Cleaner.Cleanable cleanable;

    @Override
    public @NotNull Renderer getRenderer() {
        if (this.renderer == null) {
            synchronized (this) {
                if (this.renderer == null) {
                    var rendererHandle = SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_CreateGPURenderer(0, this.nativeState.handle)
                    );

                    this.renderer = new GPURenderer(rendererHandle);
                }
            }
        }

        assert this.renderer != null;
        return this.renderer;
    }

    @Override
    public @NotNull Size clientSize() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to get client size of a closed window");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(2);

            IntBuffer w = buffer.slice(0, 1);
            IntBuffer h = buffer.slice(1, 1);

            SdlOperationFailedException.throwOnFailure(
                SDLVideo.SDL_GetWindowSize(this.nativeState.handle, w, h)
            );

            return new Size(w.get(0), h.get(0));
        }
    }

    public NativeWindow(long handle) {
        this.nativeState = new NativeState(handle);
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
    }

    @Override
    public void startTextInput() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to start text input on a closed window");
        }
        SdlOperationFailedException.throwOnFailure(SDLKeyboard.SDL_StartTextInput(this.nativeState.handle));
    }

    @Override
    public void stopTextInput() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to stop text input on a closed window");
        }
        SdlOperationFailedException.throwOnFailure(SDLKeyboard.SDL_StopTextInput(this.nativeState.handle));
    }

    @Override
    public void close() {
        if (this.nativeState.cleaned.getAcquire()) {
            return;
        }
        if (this.renderer != null) {
            this.renderer.close();
        }

        this.cleanable.clean();
    }
}
