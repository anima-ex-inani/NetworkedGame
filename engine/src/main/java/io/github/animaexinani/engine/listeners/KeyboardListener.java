package io.github.animaexinani.engine.listeners;

import java.util.EventListener;

public interface KeyboardListener extends EventListener {
    // fired the moment the physical key is pressed
    void onKeyDown(int scancode);

    // fired the moment the physical key is released
    void onKeyUp(int scancode);
}