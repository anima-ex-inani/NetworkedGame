package io.github.animaexinani.engine.internal.video;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLRender;
import org.lwjgl.system.MemoryStack;

import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowCreationFailedException;
import io.github.animaexinani.engine.windowing.WindowFactory;
import io.github.animaexinani.engine.windowing.WindowOptions;

public final class VideoSubsystem implements AutoCloseable, WindowFactory {
    private static final class State implements Runnable {
        private AtomicBoolean cleaned;
        
        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_VIDEO);
        }

        public State() {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_VIDEO)
            );
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private State state;
    private Cleaner.Cleanable cleanable;

    @Override
    public Window createWindow(WindowOptions options) {
        try (var stack = MemoryStack.stackPush()) {
            var windowHandleBuffer = stack.mallocPointer(1);
            var rendererHandleBuffer = stack.mallocPointer(1);

            try {
                SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_CreateWindowAndRenderer(
                        options.getTitle(),
                        options.getClientWidth(),
                        options.getClientHeight(),
                        options.getWindowFlags(),
                        windowHandleBuffer,
                        rendererHandleBuffer
                    )
                );
            }
            catch (SdlOperationFailedException e) {
                throw new WindowCreationFailedException("Failed to create window", e);
            }

            var windowHandle = windowHandleBuffer.get();
            var rendererHandle = rendererHandleBuffer.get();

            return new WindowWithRenderer(windowHandle, rendererHandle);
        }
    }

    public VideoSubsystem() {
        this.state = new State();
        this.cleanable = GlobalCleaner.register(this, this.state);
    }

    @Override
    public void close() throws Exception {
        this.cleanable.clean();
    }
}
