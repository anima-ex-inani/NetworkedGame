package io.github.animaexinani.engine.windowing;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLVideo;

public final class WindowOptions {
    private String title;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private int clientWidth;

    public int getClientWidth() {
        return this.clientWidth;
    }

    public void setClientWidth(int clientWidth) {
        this.clientWidth = clientWidth;
    }

    private int clientHeight;

    public int getClientHeight() {
        return this.clientHeight;
    }

    public void setClientHeight(int clientHeight) {
        this.clientHeight = clientHeight;
    }

    private long windowFlags;

    public long getWindowFlags() {
        return this.windowFlags;
    }

    public boolean isResizable() {
        return (this.windowFlags & SDLVideo.SDL_WINDOW_RESIZABLE) == SDLVideo.SDL_WINDOW_RESIZABLE;
    }

    public void setResizable(boolean resizable) {
        if (resizable) {
            this.windowFlags |= SDLVideo.SDL_WINDOW_RESIZABLE;
        }
        else {
            this.windowFlags &= ~SDLVideo.SDL_WINDOW_RESIZABLE;
        }
    }

    public WindowOptions(@NotNull String title, int clientWidth, int clientHeight) {
        this.title = title;
        this.clientWidth = clientWidth;
        this.clientHeight = clientHeight;
        this.windowFlags = 0;
    }
}
