package io.github.animaexinani.game;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.input.InputBindings;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowOptions;
import io.github.animaexinani.game.assets.ResourceLoader;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.assets.AssetKey;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main application class for the Networked Game.
 */
public final class NetworkedGame extends Application {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;

    // State management
    private final GameStateManager stateManager;
    private final SettingsManager settingsManager;

    // instantiate Input System
    private final GameInputListener inputListener;
    private final RebindingController rebindingController;

    private static final ApplicationOptions OPTIONS = new ApplicationOptions("Networked Game", "0.1.0-alpha.5",
            "io.github.animaexinani.networkedgame");

    private long lastTime = 0;
    private double accumulator = 0.0;

    private static final double TIME_STEP = 1.0 / 60.0;

    @Override
    protected boolean iterate() {
        long currentTime = System.nanoTime();
        // divide by 1 billion to convert nanoseconds to seconds
        float frameTime = (currentTime - this.lastTime) / 1_000_000_000.0f;
        this.lastTime = currentTime;

        // prevent the "Spiral of Death" if the window is dragged or minimized
        if (frameTime > 0.25f)
            frameTime = 0.25f;

        // pour that time into our bucket
        this.accumulator += frameTime;

        while (this.accumulator >= TIME_STEP) {
            Duration dt = Duration.ofNanos((long) (1_000_000_000L * TIME_STEP));

            this.stateManager.handleInput(this.inputListener, dt);
            this.stateManager.update(dt);

            // remove one tick's worth of time from the bucket
            this.accumulator -= TIME_STEP;
        }

        // the Render Loop
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);

        this.stateManager.render(renderer);

        renderer.present();
        return true;
    }

    @Override
    public void close() {
        try {
            this.mainWindow.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "Unhandled exception when closing window");
        }

        super.close();
    }

    /**
     * Entry point for the application.
     */
    public static void main() {
        try (var game = new NetworkedGame()) {
            game.run();
        }
    }

    /**
     * Creates a new NetworkedGame.
     */
    public NetworkedGame() {
        super(NetworkedGame.OPTIONS);

        var windowOptions = new WindowOptions("Networked Game", 1920, 1080);
        windowOptions.setResizable(true);

        var windowFactory = super.windowFactory();
        this.mainWindow = windowFactory.createWindow(windowOptions);

        this.assetManager().registerLoader(new ResourceLoader());

        this.settingsManager = new SettingsManager();

        // actually create the InputSystem object in memory
        var settings = this.settingsManager.getSettings();
        // Always start with default bindings, then override with custom bindings from settings
        InputBindings bindings = InputBindings.defaultBindings();
        settings.getKeybinds().forEach((actionName, scancode) -> {
            try {
                bindings.bind(scancode, io.github.animaexinani.engine.input.GameAction.valueOf(actionName));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Unknown game action in settings: " + actionName);
            }
        });
        // Save complete bindings to settings to ensure file is always up-to-date
        bindings.getAll().forEach((scancode, action) -> 
            settings.getKeybinds().put(action.name(), scancode));
        this.settingsManager.save();

        this.inputListener = new GameInputListener(bindings);
        this.rebindingController = new RebindingController(bindings);

        // tell the engine to send key presses to the inputSystem, not 'this'
        this.eventRegistry().register(KeyboardListener.class, this.inputListener);
        this.eventRegistry().register(KeyboardListener.class, this.rebindingController);

        this.stateManager = new GameStateManager();

        FontFace fontFace;
        try {
            fontFace = this.assetManager().load(new AssetKey<>(FontFace.class, "/test.ttf")).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to load UI font: interrupted", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Failed to load UI font", e);
        }

        // Initialize with MainMenuState
        this.stateManager.transitionTo(new MainMenuState(this.mainWindow, this.stateManager, fontFace, this.eventRegistry(), this.settingsManager, this.rebindingController));

        // reset the clock right before the constructor finishes!
        this.lastTime = System.nanoTime();
    }
}
