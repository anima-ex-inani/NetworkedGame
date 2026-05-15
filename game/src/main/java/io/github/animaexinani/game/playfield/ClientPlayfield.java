package io.github.animaexinani.game.playfield;

import java.time.Duration;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.game.nentities.Entity;

/**
 * A client-side representation of the game's playfield.
 * This interface focuses on rendering entities and managing their visual
 * representations.
 */
public interface ClientPlayfield extends Playfield {
    /**
     * Handles input for the local player's entity.
     * 
     * @param input The input listener to query for held actions
     * @param delta The time that has passed since the last update
     */
    void handleInput(@NotNull GameInputListener input, @NotNull Duration delta);

    /**
     * Gets the entity currently controlled by the local player.
     * 
     * @return The local player's entity
     */
    @NotNull
    Entity localPlayer();

    /**
     * Renders all entities in the playfield that have a registered visual
     * representation.
     * 
     * @param renderer The renderer to use for drawing
     */
    void render(@NotNull Renderer renderer);

    /**
     * Associates a visual drawable component with an entity's unique identifier.
     * If a visual is already registered for the given ID, it will be overwritten.
     * 
     * @param entityId The UUID of the entity
     * @param drawable The visual representation to associate with the entity. If
     *                 null, the entity's visuals will be removed.
     * @return <code>true</code> if the entity's visuals were changed,
     *         <code>false</code> otherwise
     */
    boolean registerVisuals(@NotNull UUID entityId, @Nullable Drawable drawable);

    /**
     * Removes the visual representation associated with an entity's unique
     * identifier.
     * 
     * @param entityId The UUID of the entity whose visuals should be removed
     * @return <code>true</code> if the entity's visuals were changed,
     *         <code>false</code> otherwise
     */
    boolean removeVisuals(@NotNull UUID entityId);
}
