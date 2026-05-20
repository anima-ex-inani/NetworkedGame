package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;

/**
 * The main menu of the game.
 */
public class MainMenuState extends BaseMenuState {

    /**
     * Creates a new MainMenuState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     */
    public MainMenuState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float centerX = 1920 / 2.0f;
        float startY = 300;
        float spacing = 100;

        this.components.add(this.createButton("Singleplayer", centerX, startY, () -> {
            this.stateManager.transitionTo(new SingleplayerMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
        this.components.add(this.createButton("Multiplayer", centerX, startY + spacing, () -> {
            this.stateManager.transitionTo(new MultiplayerMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
        this.components.add(this.createButton("Settings", centerX, startY + 2 * spacing, () -> {
            this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
        this.components.add(this.createButton("Quit", centerX, startY + 3 * spacing, () -> {
            System.exit(0);
        }));
    }
}
