package io.github.animaexinani.game.nentities;

import java.util.EventListener;

import org.jetbrains.annotations.NotNull;

/**
 * An event listener that is notified when an entity takes damage.
 */
@FunctionalInterface
public interface DamageTakenEventListener extends EventListener {
    /**
     * The method that is called when an entity takes damage
     * @param entity The entity that took damage
     * @param healthDamage The amount of health damage that was taken
     * @param shieldDamage The amount of shield damage that was taken
     * @param lethal Whether the damage was lethal
     */
    void onDamageTaken(@NotNull Entity entity, int healthDamage, int shieldDamage, boolean lethal);
}
