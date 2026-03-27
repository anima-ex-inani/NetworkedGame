package io.github.animaexinani.engine.internal;

import org.lwjgl.sdl.SDLError;

public final class SdlOperationFailedException extends RuntimeException {
    public static void throwOnFailure(boolean result) {
        if (result) {
            return;
        }

        throw new SdlOperationFailedException(SDLError.SDL_GetError());
    }
    
    public SdlOperationFailedException(String message) {
        super(message);
    }
}
