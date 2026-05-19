package io.github.animaexinani.game;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.nentities.Asteroid;
import io.github.animaexinani.game.nentities.ClientNetworkEntity;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.nentities.ServerNetworkEntity;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.server.GameClient;
import io.github.animaexinani.game.server.GameServer;
import io.github.animaexinani.game.util.UUIDGenerator;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
     * Creates a new PlayState.
     * @param window the game window
     * @param fontFace the font to use for UI
     * @param stateManager the state manager
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     * @param mode the mode (LOCAL or CLIENT)
     * @param host the server host (if CLIENT)
     * @param port the server port
     */
    public PlayState(Window window, FontFace fontFace, GameStateManager stateManager, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController, NetworkedGame.Mode mode, String host, int port) {
        this.window = window;
        this.fontFace = fontFace;
        this.stateManager = stateManager;
        this.eventRegistry = eventRegistry;
        this.settingsManager = settingsManager;
        this.rebindingController = rebindingController;

        float width = window != null ? window.clientSize().width() : 1920.0f;
        float height = window != null ? window.clientSize().height() : 1080.0f;
        var sizeF = new SizeF(width, height);

        // --- Server setup (LOCAL mode) ---
        if (mode == NetworkedGame.Mode.LOCAL) {
            List<Entity> serverEntities = new ArrayList<>();
            Random rand = new Random();
            for (int i = 0; i < 5; i++) {
                float x = rand.nextFloat() * sizeF.width();
                float y = rand.nextFloat() * sizeF.height();
                double vx = rand.nextDouble() * 100 - 50;
                double vy = rand.nextDouble() * 100 - 50;
                serverEntities.add(new Asteroid(EntityType.ASTEROID, x, y, vx, vy));
            }

            UUID serverId = new UUID(0L, 0L);
            ServerNetworkEntity serverDummy = new ServerNetworkEntity(serverId, EntityType.PLAYER);
            serverEntities.add(serverDummy);

            CombinedWorld serverWorld = new CombinedWorld.Builder()
                    .withEntities(serverEntities)
                    .withLocalPlayerId(serverId)
                    .withSize(sizeF)
                    .build();

            this.gameServer = new GameServer(serverWorld, port);
            this.gameServer.start();
        } else {
            this.gameServer = null;
        }

        // --- Client setup ---
        this.myPlayerId = UUIDGenerator.generateV7Uuid();
        ClientNetworkEntity localDummy = new ClientNetworkEntity(this.myPlayerId, EntityType.PLAYER);

        this.combinedWorld = new CombinedWorld.Builder()
                .withEntity(localDummy)
                .withLocalPlayerId(this.myPlayerId)
                .withSize(sizeF)
                .withVisualFactory(EntityType.BULLET, entity -> new ConvexPolygon(
                        new PointF[]{
                            new PointF(5.0f, 0.0f),
                            new PointF(-2.0f, 2.5f),
                            new PointF(-2.0f, -2.5f)
                        }, Color.WHITE))
                .withVisualFactory(EntityType.ASTEROID, entity -> new ConvexPolygon(
                        Asteroid.getAsteroidLocalPointsForType(EntityType.ASTEROID)
                                .toArray(PointF[]::new),
                        new Color(0.6f, 0.6f, 0.6f, 1.0f)))
                .withVisualFactory(EntityType.PLAYER, entity -> {
                    Color c = new Color(0.0f, 1.0f, 0.0f, 1.0f);
                    if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                    return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                })
                .withVisualFactory(EntityType.SCOUT_DRONE, entity -> {
                    Color c = new Color(1.0f, 1.0f, 0.0f, 1.0f);
                    if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                    return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                })
                .withVisualFactory(EntityType.STRIKE_FIGHTER, entity -> {
                    Color c = new Color(1.0f, 0.0f, 1.0f, 1.0f);
                    if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                    return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                })
                .build();
        
        this.gameClient = new GameClient(this.combinedWorld, this.myPlayerId);
        this.gameClient.connect(host, port);

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
        if (event.action() == KeyEvent.Action.PRESS && event.scancode() == 41) { // 41 is ESCAPE in SDL scancodes
            this.stateManager.transitionTo(new PauseState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this, this.settingsManager, this.rebindingController));
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
