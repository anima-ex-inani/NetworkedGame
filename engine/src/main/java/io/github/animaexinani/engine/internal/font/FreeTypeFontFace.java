package io.github.animaexinani.engine.internal.font;

import io.github.animaexinani.engine.font.Font;
import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.FontStyle;
import io.github.animaexinani.engine.font.FontWeight;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class FreeTypeFontFace implements FontFace, AutoCloseable {
    private static final class NativeState implements Runnable {
        private final long faceHandle;
        private final AtomicBoolean cleaned;

        public NativeState(long faceHandle) {
            this.faceHandle = faceHandle;
            this.cleaned = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            this.cleaned.setRelease(true);
            FT_Face face = FT_Face.create(this.faceHandle);
            FreeType.FT_Done_Face(face);
        }
    }

    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;

    public FreeTypeFontFace(@NotNull ByteBuffer fontData, @NotNull FreetypeLibrary library) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer aface = stack.mallocPointer(1);
            int err = FreeType.FT_New_Memory_Face(library.libraryHandle(), fontData, 0, aface);
            if (err != 0) {
                throw new RuntimeException("Failed to create FreeType face: error " + err);
            }
            this.nativeState = new NativeState(aface.get(0));
            this.cleanable = GlobalCleaner.register(this, this.nativeState);
        }
    }

    @Override
    public Font fontAt(float pixelSize, FontWeight weight, FontStyle style) {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("FontFace is closed");
        }
        return new FreeTypeFont(this.nativeState.faceHandle, pixelSize, weight, style);
    }

    @Override
    public boolean isValid() {
        return !this.nativeState.cleaned.getAcquire();
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }
}
