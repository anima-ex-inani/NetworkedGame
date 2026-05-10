package io.github.animaexinani.engine.internal.render;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.internal.video.NativeTexture;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.RenderingOperationFailedException;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.PixelFormat;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.texture.TextureCreationException;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;
import io.github.animaexinani.engine.vertex.Vertex;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GPURenderer implements Renderer {
    private static final class NativeState implements Runnable {
        private final long handle;

        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLRender.SDL_DestroyRenderer(this.handle);
        }

        public NativeState(long handle) {
            this.handle = handle;
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private final NativeState nativeState;

    private final Cleaner.Cleanable cleanable;

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
            var nativeTexture = SdlOperationFailedException.throwOnFailure(SDLRender.SDL_CreateTextureFromSurface(this.nativeState.handle, surface));
            return new NativeTexture(nativeTexture);
        } catch (SdlOperationFailedException e) {
            throw new TextureCreationException("Failed to create texture", e);
        }
    }

    @Override
    public void clear(@NotNull Color color) {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to clear the back buffer of a closed renderer");
        }

        Objects.requireNonNull(color);

        float red = color.red();
        float green = color.green();
        float blue = color.blue();
        float alpha = color.alpha();

        try {
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_SetRenderDrawColorFloat(this.nativeState.handle, red, green, blue, alpha)
            );
        } catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to set clear color", e);
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_RenderClear(this.nativeState.handle)
            );
        } catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to clear back buffer", e);
        }
    }

    @Override
    public void draw(@NotNull Drawable drawable) {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to render with closed renderer");
        }

        Objects.requireNonNull(drawable);

        Texture texture = drawable.texture();
        SDL_Texture nativeTexture;
        if (texture instanceof NativeTexture texture1) {
            nativeTexture = texture1.getBackingTexture();
        } else if (Objects.isNull(texture)) {
            nativeTexture = null;
        } else {
            throw new IllegalArgumentException("Unsupported texture type: " + texture.getClass().getName());
        }

        int vertexCount = drawable.vertexCount();
        int indexCount = drawable.indexCount();


        // materialize vertices at once to prevent drawable.vertexAt() recalls
        Vertex[] vertexCache = new Vertex[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            vertexCache[i] = drawable.vertexAt(i);
        }

        try (var stack = MemoryStack.stackPush()) {
            FloatBuffer xy = stack.mallocFloat(vertexCount * 2);
            for (int i = 0; i < vertexCount; i++) {
                var pos = vertexCache[i].position();
                xy.put(i * 2, pos.x());
                xy.put(i * 2 + 1, pos.y());
            }

            FloatBuffer uv;
            int uvStride;
            if (Objects.isNull(texture)) {
                uv = null;
                uvStride = 0;
            } else {
                uv = stack.mallocFloat(vertexCount * 2);
                uvStride = Float.BYTES * 2;
            }

            var color = SDL_FColor.calloc(vertexCount, stack);
            for (int i = 0; i < vertexCount; i++) {
                var v = vertexCache[i];
                var currentColor = color.position(i);
                currentColor.r(v.color().red());
                currentColor.g(v.color().green());
                currentColor.b(v.color().blue());
                currentColor.a(v.color().alpha());
                if (!Objects.isNull(uv)) {
                    var vertexUv = texture.getUvOfPoint(v.uv());
                    uv.put(i * 2, vertexUv.x());
                    uv.put(i * 2 + 1, vertexUv.y());
                }
            }
            color.position(0);

            IntBuffer indices = stack.mallocInt(indexCount);
            for (int i = 0; i < indexCount; i++) {
                indices.put(i, drawable.indexAt(i));
            }

            try {
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderGeometryRaw(
                                this.nativeState.handle,
                                nativeTexture,
                                xy,
                                Float.BYTES * 2,
                                color,
                                SDL_FColor.SIZEOF,
                                uv,
                                uvStride,
                                vertexCount,
                                indices,
                                indexCount,
                                Integer.BYTES
                        )
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to render object", e);
            }
        }
    }

    @Override
    public void present() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to present the back buffer of a closed renderer");
        }

        try {
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_RenderPresent(this.nativeState.handle)
            );
        } catch (SdlOperationFailedException e) {
            throw new RenderingOperationFailedException("Failed to present back buffer", e);
        }
    }

    public GPURenderer(long handle) {
        this.nativeState = new NativeState(handle);
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }
}