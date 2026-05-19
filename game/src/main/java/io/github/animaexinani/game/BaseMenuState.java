package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.ui.UIComponent;
import io.github.animaexinani.engine.ui.UITextField;
import io.github.animaexinani.engine.listeners.MouseDownListener;
import io.github.animaexinani.engine.listeners.MouseMoveListener;
import io.github.animaexinani.engine.listeners.TextInputListener;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for menu states that provides UI component management and event handling.
 */
public abstract class BaseMenuState implements GameState, MouseDownListener, MouseMoveListener, KeyboardListener, TextInputListener {
    protected final List<UIComponent> components = new ArrayList<>();
    protected final GameStateManager stateManager;
    protected final FontFace fontFace;
    protected final EventRegistry eventRegistry;
    protected final Window window;
    protected final SettingsManager settingsManager;

    /**
     * Creates a new BaseMenuState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use for UI
     * @param eventRegistry the event registry to register listeners
     * @param settingsManager the settings manager
     */
    public BaseMenuState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager) {
        this.window = window;
        this.stateManager = stateManager;
        this.fontFace = fontFace;
        this.eventRegistry = eventRegistry;
        this.settingsManager = settingsManager;
    }

    @Override
    public void enter() {
        this.eventRegistry.register(MouseDownListener.class, this);
        this.eventRegistry.register(MouseMoveListener.class, this);
        this.eventRegistry.register(KeyboardListener.class, this);
        this.eventRegistry.register(TextInputListener.class, this);
    }

    @Override
    public void exit() {
        this.eventRegistry.remove(MouseDownListener.class, this);
        this.eventRegistry.remove(MouseMoveListener.class, this);
        this.eventRegistry.remove(KeyboardListener.class, this);
        this.eventRegistry.remove(TextInputListener.class, this);
        if (this.window != null) {
            this.window.stopTextInput();
        }
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
        boolean anyFieldFocused = false;
        for (var component : this.components) {
            if (component instanceof UIButton btn) {
                btn.handleMouseDown(x, y);
            }
            if (component instanceof UITextField field) {
                boolean wasFocused = field.focused();
                boolean nowFocused = field.contains(x, y);
                field.focused(nowFocused);
                if (nowFocused) anyFieldFocused = true;
            }
        }

        if (this.window != null) {
            if (anyFieldFocused) {
                this.window.startTextInput();
            } else {
                this.window.stopTextInput();
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

    @Override
    public void onTextInput(String text) {
        for (var component : this.components) {
            if (component instanceof UITextField field) {
                field.handleTextInput(text);
            }
        }
    }

    /**
     * Helper method to create a button with standard styling and positioning.
     * @param label the text to display on the button
     * @param x the center x-coordinate
     * @param y the center y-coordinate
     * @param onClick the action to perform when clicked
     * @return the created UIButton
     */
    protected UIButton createButton(String label, float x, float y, Runnable onClick) {
        Text text = new Text(this.fontFace, label);
        text.fontSize(32.0f);
        text.color(Color.WHITE);
        text.origin(TextOrigin.CENTER);
        
        UIButton button = new UIButton(text, onClick);
        button.position(new PointF(x - 150, y - 25));
        button.size(new SizeF(300, 50));
        return button;
    }
}
