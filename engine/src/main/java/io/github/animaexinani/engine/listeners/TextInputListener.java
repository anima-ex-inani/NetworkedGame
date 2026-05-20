package io.github.animaexinani.engine.listeners;

import java.util.EventListener;

/**
 * Functional interface for listening to text input events.
 */
@FunctionalInterface
public interface TextInputListener extends EventListener {
    /**
     * Called when text is input.
     *
     * @param text the input text (usually a single character or a short string)
     */
    void onTextInput(String text);
}
