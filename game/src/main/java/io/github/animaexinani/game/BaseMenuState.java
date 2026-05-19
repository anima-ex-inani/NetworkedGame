package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.ui.UIComponent;
import io.github.animaexinani.engine.ui.UITextField;
import io.github.animaexinani.engine.listeners.MouseDownListener;
import io.github.animaexinani.engine.listeners.MouseMoveListener;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.EventRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for menu states that provides UI component management and event handling.
 */
public abstract class BaseMenuState implements GameState, MouseDownListener, MouseMoveListener, KeyboardListener {
    protected final List<UIComponent> components = new ArrayList<>();
    protected final GameStateManager stateManager;
    protected final FontFace fontFace;
    protected final EventRegistry eventRegistry;

    /**
     * Creates a new BaseMenuState.
     * @param stateManager the state manager
     * @param fontFace the font to use for UI
     * @param eventRegistry the event registry to register listeners
     */
    public BaseMenuState(GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry) {
        this.stateManager = stateManager;
        this.fontFace = fontFace;
        this.eventRegistry = eventRegistry;
    }

    @Override
    public void enter() {
        this.eventRegistry.register(MouseDownListener.class, this);
        this.eventRegistry.register(MouseMoveListener.class, this);
        this.eventRegistry.register(KeyboardListener.class, this);
    }

    @Override
    public void exit() {
        this.eventRegistry.remove(MouseDownListener.class, this);
        this.eventRegistry.remove(MouseMoveListener.class, this);
        this.eventRegistry.remove(KeyboardListener.class, this);
    }

    @Override
    public void update(Duration dt) {}

    @Override
    public void render(Renderer renderer) {
        for (var component : this.components) {
            component.render(renderer);
        }
    }

    @Override
    public void handleInput(GameInputListener inputListener, Duration dt) {}

    @Override
    public void onMouseDown(int button, float x, float y) {
        for (var component : this.components) {
            if (component instanceof UIButton btn) {
                btn.handleMouseDown(x, y);
            }
            if (component instanceof UITextField field) {
                field.focused(field.contains(x, y));
            }
        }
    }

    @Override
    public void onMouseMove(float x, float y) {
        for (var component : this.components) {
            if (component instanceof UIButton btn) {
                btn.handleMouseMove(x, y);
            }
        }
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        for (var component : this.components) {
            if (component instanceof UITextField field) {
                field.handleKeyEvent(event);
            }
        }
    }
}
