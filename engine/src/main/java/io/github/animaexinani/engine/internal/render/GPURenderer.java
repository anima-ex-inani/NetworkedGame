package io.github.animaexinani.engine.internal.render;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.internal.video.NativeTexture;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rectangle.Rect;
import io.github.animaexinani.engine.rectangle.RectF;
import io.github.animaexinani.engine.rendering.RenderContext;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.RenderingOperationFailedException;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.LazyTexture;
import io.github.animaexinani.engine.texture.PixelFormat;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.texture.TextureCreationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private final RenderContext context;

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

        drawable.draw(this.context);
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
        this.context = new DefaultRenderContext();
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }

    private final class DefaultRenderContext implements RenderContext {
        @Override
        public @NotNull Size framebufferSize() {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_GetRenderOutputSize(GPURenderer.this.nativeState.handle, w, h)
                );
                return new Size(w.get(0), h.get(0));
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to get framebuffer size", e);
            }
        }

        @Override
        public void renderGeometry(@NotNull Vertex @NotNull [] vertices, int @NotNull [] indices, @Nullable Texture texture) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            SDL_Texture nativeTexture;
            if (texture instanceof LazyTexture lazyTex) {
                texture = lazyTex.getOrCreateNativeTexture(GPURenderer.this);
            }

            if (texture instanceof NativeTexture texture1) {
                nativeTexture = texture1.getBackingTexture();
            } else if (Objects.isNull(texture)) {
                nativeTexture = null;
            } else {
                throw new IllegalArgumentException("Unsupported texture type: " + texture.getClass().getName());
            }

            int vertexCount = vertices.length;
            int indexCount = indices.length;

            try (var stack = MemoryStack.stackPush()) {
                FloatBuffer xy = stack.mallocFloat(vertexCount * 2);
                for (int i = 0; i < vertexCount; i++) {
                    var pos = vertices[i].position();
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
                    var v = vertices[i];
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

                IntBuffer indexBuffer = stack.mallocInt(indexCount);
                for (int i = 0; i < indexCount; i++) {
                    indexBuffer.put(i, indices[i]);
                }

                try {
                    SdlOperationFailedException.throwOnFailure(
                            SDLRender.SDL_RenderGeometryRaw(
                                    GPURenderer.this.nativeState.handle,
                                    nativeTexture,
                                    xy,
                                    Float.BYTES * 2,
                                    color,
                                    SDL_FColor.SIZEOF,
                                    uv,
                                    uvStride,
                                    vertexCount,
                                    indexBuffer,
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
        public void drawPoint(@NotNull PointF p, @NotNull Color color) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try {
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderDrawColorFloat(GPURenderer.this.nativeState.handle, color.red(), color.green(), color.blue(), color.alpha())
                );
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderPoint(GPURenderer.this.nativeState.handle, p.x(), p.y())
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to draw point", e);
            }
        }

        @Override
        public void drawPoints(@NotNull PointF @NotNull [] points, @NotNull Color color) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                var sdlPoints = SDL_FPoint.malloc(points.length, stack);
                for (int i = 0; i < points.length; i++) {
                    sdlPoints.get(i).set(points[i].x(), points[i].y());
                }
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderDrawColorFloat(GPURenderer.this.nativeState.handle, color.red(), color.green(), color.blue(), color.alpha())
                );
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderPoints(GPURenderer.this.nativeState.handle, sdlPoints)
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to draw points", e);
            }
        }

        @Override
        public void drawLine(@NotNull PointF p1, @NotNull PointF p2, @NotNull Color color) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try {
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderDrawColorFloat(GPURenderer.this.nativeState.handle, color.red(), color.green(), color.blue(), color.alpha())
                );
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderLine(GPURenderer.this.nativeState.handle, p1.x(), p1.y(), p2.x(), p2.y())
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to draw line", e);
            }
        }

        @Override
        public void drawLines(@NotNull PointF @NotNull [] points, @NotNull Color color) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                var sdlPoints = SDL_FPoint.malloc(points.length, stack);
                for (int i = 0; i < points.length; i++) {
                    sdlPoints.get(i).set(points[i].x(), points[i].y());
                }
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderDrawColorFloat(GPURenderer.this.nativeState.handle, color.red(), color.green(), color.blue(), color.alpha())
                );
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderLines(GPURenderer.this.nativeState.handle, sdlPoints)
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to draw lines", e);
            }
        }

        @Override
        public void drawRect(float x, float y, float width, float height, @NotNull Color color) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                var rect = SDL_FRect.malloc(stack);
                rect.x(x);
                rect.y(y);
                rect.w(width);
                rect.h(height);
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderDrawColorFloat(GPURenderer.this.nativeState.handle, color.red(), color.green(), color.blue(), color.alpha())
                );
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderRect(GPURenderer.this.nativeState.handle, rect)
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to draw rect", e);
            }
        }

        @Override
        public void fillRect(float x, float y, float width, float height, @NotNull Color color) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                var rect = SDL_FRect.malloc(stack);
                rect.x(x);
                rect.y(y);
                rect.w(width);
                rect.h(height);
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderDrawColorFloat(GPURenderer.this.nativeState.handle, color.red(), color.green(), color.blue(), color.alpha())
                );
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderFillRect(GPURenderer.this.nativeState.handle, rect)
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to fill rect", e);
            }
        }

        @Override
        public void drawTexture(@NotNull Texture texture, float x, float y) {
            var size = texture.getSize();
            this.drawTexture(texture, x, y, size.width(), size.height());
        }

        @Override
        public void drawTexture(@NotNull Texture texture, float x, float y, float width, float height) {
            this.drawTexture(texture, null, new RectF(x, y, width, height));
        }

        @Override
        public void drawTexture(@NotNull Texture texture, @Nullable Rect src, @Nullable RectF dst) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            SDL_Texture nativeTexture;
            if (texture instanceof LazyTexture lazyTex) {
                texture = lazyTex.getOrCreateNativeTexture(GPURenderer.this);
            }

            if (texture instanceof NativeTexture texture1) {
                nativeTexture = texture1.getBackingTexture();
            } else {
                throw new IllegalArgumentException("Unsupported texture type: " + texture.getClass().getName());
            }

            try (var stack = MemoryStack.stackPush()) {
                SDL_FRect sdlSrc = null;
                if (src != null) {
                    sdlSrc = SDL_FRect.malloc(stack);
                    sdlSrc.x(src.left());
                    sdlSrc.y(src.top());
                    sdlSrc.w(src.width());
                    sdlSrc.h(src.height());
                }

                SDL_FRect sdlDst = null;
                if (dst != null) {
                    sdlDst = SDL_FRect.malloc(stack);
                    sdlDst.x(dst.left());
                    sdlDst.y(dst.top());
                    sdlDst.w(dst.width());
                    sdlDst.h(dst.height());
                }

                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_RenderTexture(GPURenderer.this.nativeState.handle, nativeTexture, sdlSrc, sdlDst)
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to draw texture", e);
            }
        }

        @Override
        public void clipRect(@Nullable Rect rect) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                SDL_Rect sdlRect = null;
                if (rect != null) {
                    sdlRect = SDL_Rect.malloc(stack);
                    sdlRect.x(rect.left());
                    sdlRect.y(rect.top());
                    sdlRect.w(rect.width());
                    sdlRect.h(rect.height());
                }
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderClipRect(GPURenderer.this.nativeState.handle, sdlRect)
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to set clip rect", e);
            }
        }

        @Override
        public @Nullable Rect clipRect() {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                var sdlRect = SDL_Rect.malloc(stack);
                if (SDLRender.SDL_GetRenderClipRect(GPURenderer.this.nativeState.handle, sdlRect)) {
                    return new Rect(sdlRect.x(), sdlRect.y(), sdlRect.w(), sdlRect.h());
                }
                return null;
            }
        }

        @Override
        public void viewport(@Nullable Rect rect) {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                SDL_Rect sdlRect = null;
                if (rect != null) {
                    sdlRect = SDL_Rect.malloc(stack);
                    sdlRect.x(rect.left());
                    sdlRect.y(rect.top());
                    sdlRect.w(rect.width());
                    sdlRect.h(rect.height());
                }
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_SetRenderViewport(GPURenderer.this.nativeState.handle, sdlRect)
                );
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to set viewport", e);
            }
        }

        @Override
        public @NotNull Rect viewport() {
            if (GPURenderer.this.nativeState.cleaned.getAcquire()) {
                throw new IllegalStateException("Attempted to use RenderContext of a closed renderer");
            }

            try (var stack = MemoryStack.stackPush()) {
                var sdlRect = SDL_Rect.malloc(stack);
                SdlOperationFailedException.throwOnFailure(
                        SDLRender.SDL_GetRenderViewport(GPURenderer.this.nativeState.handle, sdlRect)
                );
                return new Rect(sdlRect.x(), sdlRect.y(), sdlRect.w(), sdlRect.h());
            } catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to get viewport", e);
            }
        }
    }
}