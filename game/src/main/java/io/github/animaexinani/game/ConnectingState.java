package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;

import java.time.Duration;
import java.util.Random;

/**
 * Simulates a connection attempt to a multiplayer game.
 */
public class ConnectingState extends BaseMenuState {
    private final Text statusText;
    private Duration timer = Duration.ZERO;
    private static final Duration CONNECTION_DELAY = Duration.ofSeconds(3);
    private final Random random = new Random();

    /**
     * Creates a new ConnectingState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     */
    public ConnectingState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        this.statusText = new Text(fontFace, "Connecting...");
        this.statusText.fontSize(32.0f);
        this.statusText.color(Color.WHITE);
        this.statusText.origin(TextOrigin.CENTER);
        this.statusText.translation(new PointF(1920 / 2.0f, 1080 / 2.0f));

        this.components.add(this.createButton("Cancel", 1920 / 2.0f, 700, () -> {
            this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }

    @Override
    public void update(Duration dt) {
        this.timer = this.timer.plus(dt);
        if (this.timer.compareTo(CONNECTION_DELAY) >= 0) {
            if (this.random.nextBoolean()) {
                // Success: Transition to PlayState as host (simulated)
                this.stateManager.transitionTo(new PlayState(this.window, this.fontFace, this.stateManager, this.eventRegistry, this.settingsManager, this.rebindingController));
            } else {
                // Failure: Transition back (for now to main menu)
                this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
            }
        }
    }

    @Override
    public void render(io.github.animaexinani.engine.rendering.Renderer renderer) {
        renderer.draw(this.statusText);
        super.render(renderer);
    }
}
