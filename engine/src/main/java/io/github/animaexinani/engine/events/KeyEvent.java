package io.github.animaexinani.engine.events;

public record KeyEvent(int scancode, Action action) {
    
    public enum Action {
        PRESS,
        RELEASE,
        REPEAT
    }
}