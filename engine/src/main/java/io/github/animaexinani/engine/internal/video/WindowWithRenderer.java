package io.github.animaexinani.engine.internal.video;

import java.lang.ref.Cleaner.Cleanable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.animaexinani.engine.rendering.Drawable;
import io.github.animaexinani.engine.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.*;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.RenderingOperationFailedException;
import io.github.animaexinani.engine.windowing.Window;
import org.lwjgl.system.MemoryStack;

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
        float red = color.red();
        float green = color.green();
        float blue = color.blue();
        float alpha = color.alpha();

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
    public void draw(@NotNull Drawable drawable) {
        Objects.requireNonNull(drawable);

        Texture texture = drawable.getTexture();
        SDL_Texture nativeTexture;
        if (texture instanceof NativeTexture texture1) {
            nativeTexture = texture1.getBackingTexture();
        }
        else if (Objects.isNull(texture)) {
            nativeTexture = null;
        }
        else {
            throw new IllegalArgumentException("Unsupported texture type: " + texture.getClass().getName());
        }

        var vertices = drawable.getVertices();
        var drawableIndices = drawable.getIndices();

        try (var stack = MemoryStack.stackPush()) {
            FloatBuffer xy = stack.mallocFloat(vertices.length * 2);
            for (int i = 0; i < vertices.length; i++) {
                xy.put(i * 2, vertices[i].position().x());
                xy.put(i * 2 + 1, vertices[i].position().y());
            }


            var color = SDL_FColor.calloc(vertices.length, stack);
            for (int i = 0; i < vertices.length; i++) {
                var currentColor = color.position(i);
                currentColor.r(vertices[i].color().red());
                currentColor.g(vertices[i].color().green());
                currentColor.b(vertices[i].color().blue());
                currentColor.a(vertices[i].color().alpha());
            }
            color.position(0);

            var indices = IntBuffer.wrap(drawable.getIndices());

            FloatBuffer uv;
            int uvStride;
            if (Objects.isNull(texture)) {
                uv = null;
                uvStride = 0;
            }
            else {
                uv = stack.mallocFloat(vertices.length * 2);
                uvStride = 8; // float * 2

                for (int i = 0; i < vertices.length; i++) {
                    var vertexUv = texture.getUvOfPoint(vertices[i].uv());
                    uv.put(i * 2, vertexUv.x());
                    uv.put(i * 2 + 1, vertexUv.y());
                }
            }

            try {
                SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_RenderGeometryRaw(
                        this.state.rendererHandle,
                        nativeTexture,
                        xy,
                        8, // float * 2
                        color,
                        SDL_FColor.SIZEOF,
                        uv,
                        uvStride,
                        vertices.length,
                        indices,
                        drawableIndices.length,
                        4 // int
                    )
                );
            }
            catch (SdlOperationFailedException e) {
                throw new RenderingOperationFailedException("Failed to render object", e);
            }
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
