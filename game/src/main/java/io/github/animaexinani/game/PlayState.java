package io.github.animaexinani.game;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.nentities.ClientNetworkEntity;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.server.GameClient;
import io.github.animaexinani.game.server.GameServer;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.game.network.GameConnection;

import java.time.Duration;
import java.util.UUID;

/**
 * The game state representing the active gameplay.
 */
public class PlayState implements GameState, KeyboardListener {
    private final CombinedWorld combinedWorld;
    private Text entityCountText;
    private final Window window;
    private final GameStateManager stateManager;
    private final EventRegistry eventRegistry;
    private final FontFace fontFace;
    private final SettingsManager settingsManager;
    private final RebindingController rebindingController;

    private final GameServer gameServer;
    private final GameClient gameClient;
    private final UUID myPlayerId;

    /**
     * Creates a new PlayState with an existing game connection.
     * @param window the game window
     * @param fontFace the font to use for UI
     * @param stateManager the state manager
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     * @param connection the active game connection
     */
    public PlayState(Window window, FontFace fontFace, GameStateManager stateManager, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController, GameConnection connection) {
        this.window = window;
        this.fontFace = fontFace;
        this.stateManager = stateManager;
        this.eventRegistry = eventRegistry;
        this.settingsManager = settingsManager;
        this.rebindingController = rebindingController;

        this.gameServer = connection.gameServer();
        this.gameClient = connection.gameClient();
        this.combinedWorld = connection.combinedWorld();
        this.myPlayerId = connection.myPlayerId();

        if (fontFace != null) {
            this.entityCountText = new Text(fontFace, "Entities: 0");
            this.entityCountText.translation(new PointF(10.0f, 10.0f));
            this.entityCountText.origin(TextOrigin.TOP_LEFT);
            this.entityCountText.fontSize(24.0f);
            this.entityCountText.color(Color.WHITE);
        }
    }

    @Override
    public void enter() {
        this.eventRegistry.register(KeyboardListener.class, this);
    }

    @Override
    public void update(Duration dt) {
        // Server timeout check (only for remote clients)
        if (this.gameClient != null && this.gameServer == null) {
            long lastPacketTime = this.gameClient.getLastPacketTimeNanos();
            if (lastPacketTime != -1) {
                long now = System.nanoTime();
                if (now - lastPacketTime > 5_000_000_000L) { // 5 seconds
                    this.stateManager.transitionTo(new ErrorMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController, "Connection to server lost."));
                    return;
                }
            }
        }

        // flush spawn/despawn queues so newly received network entities appear
        this.combinedWorld.preUpdate(Duration.ZERO);

        // glide dummy visuals toward the latest server snapshot
        float frameTime = dt.toNanos() / 1_000_000_000.0f;
        this.combinedWorld.interpolateVisuals(frameTime);
    }

    @Override
    public void render(Renderer renderer) {
        this.combinedWorld.render(renderer);

        if (this.gameClient != null && this.myPlayerId != null) {
            Entity localPlayer = this.combinedWorld.getEntity(this.myPlayerId);
            
            if (localPlayer instanceof ClientNetworkEntity cp) {
                var currentSize = this.window.clientSize();

                float currentWidth = currentSize.width();
                float currentHeight = currentSize.height();

                float barWidth = 400f;
                float barHeight = 20f;
                
                float startX = (currentWidth - barWidth) / 2f;
                float startY = currentHeight - 80f; // Bottom center

                float hpPercent = Math.max(0, cp.health()) / 100_000f;
                renderer.draw(this.createRect(startX, startY + 25, barWidth * hpPercent, barHeight, new Color(1f, 0f, 0f, 1f)));

                float shieldPercent = Math.max(0, cp.shield()) / 100_000f;
                renderer.draw(this.createRect(startX, startY, barWidth * shieldPercent, barHeight, new Color(0f, 0.5f, 1f, 1f)));
            }
        }

        if (this.entityCountText != null) {
            int count = this.combinedWorld.entities().size();
            this.entityCountText.text("Entities: " + count);
            renderer.draw(this.entityCountText);
        }
    }

    @Override
    public void handleInput(GameInputListener inputListener, Duration dt) {
        if (this.gameClient != null) {
            this.gameClient.sendInputs(inputListener);
        }
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        if (this.stateManager.currentState() != this) return;
        
        if (event.action() == KeyEvent.Action.PRESS && event.scancode() == 41) { // 41 is ESCAPE in SDL scancodes
            this.stateManager.pushState(new PauseState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this, this.settingsManager, this.rebindingController));
        }
    }

    @Override
    public void exit() {
        this.eventRegistry.remove(KeyboardListener.class, this);
        if (this.gameServer != null) {
            this.gameServer.stop();
        }
        if (this.gameClient != null) {
            this.gameClient.stop();
        }
    }

    private ConvexPolygon createRect(float x, float y, float width, float height, Color color) {
        return new ConvexPolygon(new PointF[] {
            new PointF(x, y), new PointF(x + width, y),
            new PointF(x + width, y + height), new PointF(x, y + height)
        }, color);
    }
}
