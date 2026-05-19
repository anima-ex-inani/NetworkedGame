package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.input.InputBindings;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.game.network.GameConnection;
import io.github.animaexinani.game.network.GameConnectionFactory;

import java.time.Duration;

/**
 * Manages the connection attempt to a multiplayer game.
 */
public class ConnectingState extends BaseMenuState {
    private final Text statusText;
    private Duration timer = Duration.ZERO;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Duration ERROR_DELAY = Duration.ofSeconds(2);
    private static final Duration PING_INTERVAL = Duration.ofMillis(500);
    private Duration pingTimer = Duration.ZERO;

    private final GameConnection connection;
    private final GameInputListener emptyInputListener;
    private boolean failed = false;

    /**
     * Creates a new ConnectingState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     * @param host the host to connect to
     * @param port the port to connect to
     */
    public ConnectingState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController, String host, int port) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float width = window != null ? window.clientSize().width() : 1920.0f;
        float height = window != null ? window.clientSize().height() : 1080.0f;
        this.connection = GameConnectionFactory.createClientConnection(new SizeF(width, height));
        this.connection.gameClient().connect(host, port);
        this.emptyInputListener = new GameInputListener(new InputBindings());

        this.statusText = new Text(fontFace, "Connecting to " + host + ":" + port + "...");
        this.statusText.fontSize(32.0f);
        this.statusText.color(Color.WHITE);
        this.statusText.origin(TextOrigin.CENTER);
        this.statusText.translation(new PointF(1920 / 2.0f, 1080 / 2.0f));

        this.components.add(this.createButton("Cancel", 1920 / 2.0f, 700, () -> {
            if (this.connection != null && this.connection.gameClient() != null) {
                this.connection.gameClient().stop();
            }
            this.stateManager.transitionTo(new JoinGameState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }

    @Override
    public void update(Duration dt) {
        if (this.failed) {
            this.timer = this.timer.plus(dt);
            if (this.timer.compareTo(ERROR_DELAY) >= 0) {
                this.stateManager.transitionTo(new JoinGameState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
            }
            return;
        }

        this.timer = this.timer.plus(dt);
        this.pingTimer = this.pingTimer.plus(dt);

        if (this.pingTimer.compareTo(PING_INTERVAL) >= 0) {
            this.pingTimer = Duration.ZERO;
            // Send empty inputs to notify server
            this.connection.gameClient().sendInputs(this.emptyInputListener);
        }

        if (this.connection.gameClient().isConnected()) {
            this.stateManager.transitionTo(new PlayState(this.window, this.fontFace, this.stateManager, this.eventRegistry, this.settingsManager, this.rebindingController, this.connection));
        } else if (this.timer.compareTo(TIMEOUT) >= 0) {
            this.failed = true;
            this.timer = Duration.ZERO;
            this.statusText.text("Connection Failed");
            this.statusText.color(new Color(1.0f, 0.0f, 0.0f, 1.0f));
            this.connection.gameClient().stop();
        }
    }

    @Override
    public void render(io.github.animaexinani.engine.rendering.Renderer renderer) {
        renderer.draw(this.statusText);
        super.render(renderer);
    }
}
