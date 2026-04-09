package io.github.animaexinani.engine.internal;

import org.jetbrains.annotations.Contract;
import org.lwjgl.sdl.SDLError;

public final class SdlOperationFailedException extends RuntimeException {
    @Contract(value = "false -> fail", pure = true)
    public static void throwOnFailure(boolean result) {
        if (result) {
            return;
        }

        throw new SdlOperationFailedException(SDLError.SDL_GetError());
    }

    public static long throwOnFailure(long result) {
        if (result == 0) {
            throw new SdlOperationFailedException(SDLError.SDL_GetError());
        }

        return result;
    }

    @Contract(value = "null -> fail; _ -> param1", pure = true)
    public static <T> T throwOnFailure(T result) {
        if (result != null) {
            return result;
        }

        throw new SdlOperationFailedException(SDLError.SDL_GetError());
    }
    
    public SdlOperationFailedException(String message) {
        super(message);
    }
}
