package io.github.animaexinani.engine.internal.video;

import java.lang.ref.Cleaner.Cleanable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rectangle.Rect;
import io.github.animaexinani.engine.rectangle.RectF;
import io.github.animaexinani.engine.rendering.BlendMode;
import io.github.animaexinani.engine.rendering.FlipMode;
import io.github.animaexinani.engine.rendering.RenderContext;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.RenderingOperationFailedException;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.texture.LazyTexture;
import io.github.animaexinani.engine.texture.PixelFormat;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.texture.TextureCreationException;
import io.github.animaexinani.engine.internal.render.SDLRenderContext;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;

public final class WindowWithRenderer implements Window, Renderer {
    private static final class NativeState implements Runnable {
        private final long windowHandle;
        private final long rendererHandle;
        private final SDL_MainThreadCallback cleanCallback;
        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            this.cleaned.setRelease(true);

            SdlOperationFailedException.throwOnFailure(
                    SDLInit.SDL_RunOnMainThread(this.cleanCallback, 0, true)
            );
            this.cleanCallback.close();
        }

        public NativeState(long windowHandle, long rendererHandle) {
            this.windowHandle = windowHandle;
            this.rendererHandle = rendererHandle;
            this.cleaned = new AtomicBoolean(false);
            this.cleanCallback = SDL_MainThreadCallback.create(_ -> {
                SDLRender.SDL_DestroyRenderer(this.rendererHandle);
                SDLVideo.SDL_DestroyWindow(this.windowHandle);
            });
        }
    }

    private final NativeState nativeState;
    private final Cleanable cleanable;
    private final RenderContext context;

    @Override
    public @NotNull Size clientSize() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to get client size of a closed window");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            SdlOperationFailedException.throwOnFailure(
                    SDLVideo.SDL_GetWindowSize(this.nativeState.windowHandle, w, h)
            );

            return new Size(w.get(0), h.get(0));
        }
    }

    @Override
    public @NotNull Texture createTexture(@NotNull Size textureSize, @NotNull PixelFormat pixelFormat, @NotNull ByteBuffer pixelBuffer) {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to create a texture after the renderer has been closed");
        }

        Objects.requireNonNull(textureSize);
        Objects.requireNonNull(pixelBuffer);

        var nativePixelFormat = switch (pixelFormat) {
            case RGBA_8888 -> SDLPixels.SDL_PIXELFORMAT_RGBA8888;
            case ARGB_8888 -> SDLPixels.SDL_PIXELFORMAT_ARGB8888;
            case BGRA_8888 -> SDLPixels.SDL_PIXELFORMAT_BGRA8888;
            case ABGR_8888 -> SDLPixels.SDL_PIXELFORMAT_ABGR8888;
            case RGBA_F16 -> SDLPixels.SDL_PIXELFORMAT_RGBA64_FLOAT;
            case ARGB_F16 -> SDLPixels.SDL_PIXELFORMAT_ARGB64_FLOAT;
            case BGRA_F16 -> SDLPixels.SDL_PIXELFORMAT_BGRA64_FLOAT;
            case ABGR_F16 -> SDLPixels.SDL_PIXELFORMAT_ABGR64_FLOAT;
            case RGBA_F32 -> SDLPixels.SDL_PIXELFORMAT_RGBA128_FLOAT;
            case ARGB_F32 -> SDLPixels.SDL_PIXELFORMAT_ARGB128_FLOAT;
            case BGRA_F32 -> SDLPixels.SDL_PIXELFORMAT_BGRA128_FLOAT;
            case ABGR_F32 -> SDLPixels.SDL_PIXELFORMAT_ABGR128_FLOAT;
        };
        var pitch = pixelFormat.calculatePitch(textureSize.width());
        try (var surface = SdlOperationFailedException.throwOnFailure(
                SDLSurface.SDL_CreateSurfaceFrom(textureSize.width(), textureSize.height(), nativePixelFormat, pixelBuffer, pitch)
        )) {
            var nativeTexture = SdlOperationFailedException.throwOnFailure(SDLRender.SDL_CreateTextureFromSurface(this.nativeState.rendererHandle, surface));
            return new NativeTexture(nativeTexture);
        }
        catch (SdlOperationFailedException e) {
            throw new TextureCreationException("Failed to create texture", e);
        }
    }

    @Override
    public void clear(@NotNull Color color) {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to clear the backbuffer of a closed renderer");
        }

        Objects.requireNonNull(color);

        float red = color.red();
        float green = color.green();
        float blue = color.blue();
        float alpha = color.alpha();

        try {
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_SetRenderDrawColorFloat(this.nativeState.rendererHandle, red, green, blue, alpha)
            );
        }
        catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to set clear color", e);
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_RenderClear(this.nativeState.rendererHandle)
            );
        }
        catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to clear back buffer", e);
        }
    }

    @Override
    public void draw(@NotNull Drawable drawable) {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to render with closed renderer");
        }

        Objects.requireNonNull(drawable);

        drawable.draw(this.context);
    }

    @Override
    public void present() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to present the backbuffer of a closed renderer");
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_RenderPresent(this.nativeState.rendererHandle)
            );
        }
        catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to present back buffer", e);
        }
    }

    @Override
    public @NotNull Renderer getRenderer() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to get renderer of a closed window");
        }

        return this;
    }

    public WindowWithRenderer(long windowHandle, long rendererHandle) {
        this.nativeState = new NativeState(windowHandle, rendererHandle);
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
        this.context = new SDLRenderContext(this, this.nativeState.rendererHandle, this.nativeState.cleaned);
    }

    @Override
    public void startTextInput() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to start text input on a closed window");
        }
        SdlOperationFailedException.throwOnFailure(SDLKeyboard.SDL_StartTextInput(this.nativeState.windowHandle));
    }

    @Override
    public void stopTextInput() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to stop text input on a closed window");
        }
        SdlOperationFailedException.throwOnFailure(SDLKeyboard.SDL_StopTextInput(this.nativeState.windowHandle));
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }
    }