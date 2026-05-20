package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.game.network.GameConnection;
import io.github.animaexinani.game.network.GameConnectionFactory;

/**
 * The singleplayer menu of the game.
 */
public class SingleplayerMenuState extends BaseMenuState {

    /**
     * Creates a new SingleplayerMenuState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     */
    public SingleplayerMenuState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float centerX = 1920 / 2.0f;
        float startY = 300;
        float spacing = 100;

        this.components.add(this.createButton("New Game", centerX, startY, () -> {
            int port = this.settingsManager.getSettings().getNetworking().getPreferredPort();
            float width = window != null ? window.clientSize().width() : 1920.0f;
            float height = window != null ? window.clientSize().height() : 1080.0f;
            GameConnection connection = GameConnectionFactory.createLocalConnection(new SizeF(width, height), port);
            this.stateManager.transitionTo(new PlayState(this.window, this.fontFace, this.stateManager, this.eventRegistry, this.settingsManager, this.rebindingController, connection));
        }));
        this.components.add(this.createButton("High Scores", centerX, startY + spacing, () -> {
            // High scores logic
        }));
        this.components.add(this.createButton("Back", centerX, startY + 2 * spacing, () -> {
            this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }
}
