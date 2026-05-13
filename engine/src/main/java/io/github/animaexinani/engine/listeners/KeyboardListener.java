package io.github.animaexinani.engine.listeners;

import java.util.EventListener; 

import io.github.animaexinani.engine.events.KeyEvent;

public interface KeyboardListener extends EventListener {
    void onKeyEvent(KeyEvent event);
}