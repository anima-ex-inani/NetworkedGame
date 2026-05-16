package io.github.animaexinani.game.nentities;

import java.util.EventListener;

import org.jetbrains.annotations.NotNull;

/**
 * An event listener for when damage is dealt to a target.
 */
@FunctionalInterface
public interface DamageDealtEventListener extends EventListener {
    /**
     * Called when damage is dealt to a target.
     * 
     * @param source the entity that dealt the damage
     * @param target the entity that received the damage
     * @param damage the amount of damage dealt
     * @param lethal whether the damage was lethal
     */
    void onDamageDealt(@NotNull Entity source, @NotNull Damageable target, int damage, boolean lethal);
}
