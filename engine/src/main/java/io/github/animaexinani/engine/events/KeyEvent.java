package io.github.animaexinani.engine.events;
import java.util.Objects;

public record KeyEvent(int scancode, Action action) {
    public KeyEvent {
        Objects.requireNonNull(action, "Action must not be null");
    }   
    
    public enum Action {
        PRESS,
        RELEASE,
        REPEAT
    }
}