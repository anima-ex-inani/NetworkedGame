package io.github.animaexinani.engine.listeners;

import java.util.EventListener;

@FunctionalInterface
public interface QuitEventListener extends EventListener {
    void onQuit();
}
